(ns anketeur.survey
  (:require
    [anketeur.filestore :as fs]
    [clojure.string :as str]
    [anketeur.util.core :as util]
    [crypto.password.bcrypt :as password]
    [clj-time.format :as fmt]
    [clj-time.core :as tm]
    [integrant.core :as ig]))

(def timestamp-formatter (fmt/formatter "yyyyMMddHHmmssSS"))

(defn str-timestamp
  "String representation of the current time"
  []
  (fmt/unparse timestamp-formatter (tm/now)))

(defn next-counter [counter]
  (let [max 1000]
    (rem ((fnil inc 0) counter) max)))

(defn counter-str [counter]
   (format "%s%03d" (str-timestamp) counter))

(defn read-app-table [env table-name]
  (fs/read-table (:app-data-dir env) table-name))

(defn view [table]
  (some-> table :data deref))

(defn flush-table [table]
  (fs/write-table table))

;; TODO try-catch and return nil in case of invalid string
(defn as-id [s]
  (when s
    (cond-> s (not (uuid? s)) java.util.UUID/fromString)))

;; read a single doc
(defn read-table-entry [table surveyno]
  (-> table
      view
      (get (as-id surveyno))))

(defn init-demo-survey! [env table]
  (when
    (and
      (= "Demo" (:env env))
      (empty? (keys (view table))))
    (let [{:keys [surveyno] :as survey-info} (util/resource-edn "edn/sample-survey.edn")]
      (when surveyno
        (swap! (:data table) assoc surveyno survey-info))))
  table)

;; Holder of state for store
(defmethod ig/init-key :anketeurweb/survey-table [_ {:keys [env]}]
  (init-demo-survey! env (read-app-table env "survey-table")))

(defmethod ig/halt-key! :anketeurweb/survey-table [_ table]
  (flush-table table))

;; use a function because partial needs the table to be mounted first
(defn read-doc [survey-table surveyno]
  (read-table-entry survey-table surveyno))

;; get a collection of docs
;; TODO add query param filter
(defn query-docs [survey-table filter-fn]
  (->> (view survey-table)
       vals
       (filter filter-fn)
       (into [])))

;; TODO caller should check if survey-info is nil, then retry
(defn insert-survey [survey-table surveyname roles]
  (let [surveyno (java.util.UUID/randomUUID)
        survey-info {:surveyname surveyname :surveyno surveyno :roles roles}]
    (swap! (:data survey-table) assoc surveyno survey-info)
    (when (= surveyno (:surveyno survey-info))
      survey-info)))

(defn upsert-survey [survey-table survey-info]
  (let [surveyno (or (-> survey-info :surveyno as-id) (java.util.UUID/randomUUID))
        uuid-survey-info (assoc survey-info :surveyno surveyno)]
    (swap! (:data survey-table) assoc surveyno uuid-survey-info)
    (when (= surveyno (:surveyno uuid-survey-info))
      uuid-survey-info)))

;; save a survey doc
(defn save-survey! [survey-table survey-info]
  ;; consider using git as a backend for the doc data.
  ;; queue up and save intermittently if autosave.
  ;; attempt to flush if saved explicitly.
  (let [upserted-survey-info (upsert-survey survey-table survey-info)]
    (flush-table survey-table)
    upserted-survey-info))

(defn update-in-survey! [survey-table [surveyno & _ :as keyvec] update-fn]
  (let [uuid (as-id surveyno)]
    (when (get (view survey-table) uuid)
      (swap! (:data survey-table) update-in keyvec update-fn)
      (flush-table survey-table)
      (get (view survey-table) uuid))))

(defmethod ig/init-key :anketeurweb/answer-table [_ {:keys [env]}]
  (read-app-table env "answer-table"))

(defmethod ig/halt-key! :anketeurweb/answer-table [_ table]
  (flush-table table))

;; use a function because partial needs the table to be mounted first
(defn read-answers [answer-table surveyno]
  (read-table-entry answer-table surveyno))

(defn read-answer-form [answer-table surveyno formno]
  (-> (read-table-entry answer-table surveyno)
      (get formno)))

(defn next-answer-counter!
  ([table surveyno formno]
   (or formno (next-answer-counter! table surveyno)))
  ([table surveyno]
   (let [keyvec [:form-counter surveyno]
         result (swap! (:data table) update-in keyvec next-counter)
         counter (get-in result keyvec)]
      (counter-str counter))))

(defn update-answers! [table surveyno {:keys [formno] :as answers}]
  (let [upsert-formno (next-answer-counter! table surveyno formno)
        answers-with-formno (assoc answers :formno upsert-formno)]
    (when upsert-formno
      (swap! (:data table) assoc-in [surveyno upsert-formno] answers-with-formno)
      upsert-formno)))

;; TODO decide where to queue write operations
(defn save-answers! [answer-table surveyno {:keys [answers]}]
  (when surveyno
    (let [formno (update-answers! answer-table (as-id surveyno) answers)]
      (flush-table answer-table)
      formno)))

(defmethod ig/init-key :anketeurweb/auth-table [_ {:keys [env]}]
  (read-app-table env "auth-table"))

(defmethod ig/halt-key! :anketeurweb/auth-table [_ table]
  (flush-table table))

(defn insert-auth [auth-table surveyname surveyno passwd]
  (when surveyno
    (let [hashkey (password/encrypt passwd)
          auth-info {:surveyname surveyname :surveyno surveyno :hashkey hashkey}]
      (swap! (:data auth-table) assoc surveyname auth-info)
      auth-info)))

(defn auth-survey [survey-table {:keys [surveyno hashkey] :as survey-info} password]
  (when (and surveyno (password/check password hashkey))
    (get (view survey-table) surveyno)))

