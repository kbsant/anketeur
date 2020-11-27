(ns anketeur.survey
  (:require
    [ashikasoft.filestore.core :as fs]
    [clojure.string :as str]
    [anketeur.util.core :as util]
    [java-time :as time]
    [integrant.core :as ig]))

(def timestamp-formatter "yyyyMMddHHmmssSS")

(defn str-timestamp
  "String representation of the current time"
  []
  (time/format timestamp-formatter (time/zoned-date-time)))

(defn next-counter [counter]
  (let [max 1000]
    (rem ((fnil inc 0) counter) max)))

(defn counter-str [counter]
   (format "%s%03d" (str-timestamp) counter))

(defn read-app-table
  "Using the APP_DATA_DIR environment variable as a base path, create or read the application data tables."
  [env table-name]
  (fs/init-store! (:app-data-dir env) table-name))

(defn view [table]
  (fs/view table))

(defn flush-table [table]
  (fs/write-store! table))

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
        (swap! (fs/data table) assoc surveyno survey-info))))
  table)

;; use a function because partial needs the table to be mounted first
(defn read-doc [{:keys [survey-table]} surveyno]
  (read-table-entry survey-table surveyno))

;; get a collection of docs
;; TODO add query param filter
(defn query-docs [{:keys [survey-table]} filter-fn]
  (->> (view survey-table)
       vals
       (filter filter-fn)
       (into [])))

;; TODO caller should check if survey-info is nil, then retry
(defn insert-survey! [{:keys [survey-table]} surveyname roles]
  (let [surveyno (java.util.UUID/randomUUID)
        survey-info {:surveyname surveyname :surveyno surveyno :roles roles}]
    (swap! (fs/data survey-table) assoc surveyno survey-info)
    (when (= surveyno (:surveyno survey-info))
      survey-info)))

(defn upsert-survey! [{:keys [survey-table]} survey-info]
  (let [surveyno (or (-> survey-info :surveyno as-id) (java.util.UUID/randomUUID))
        uuid-survey-info (assoc survey-info :surveyno surveyno)]
    (swap! (fs/data survey-table) assoc surveyno uuid-survey-info)
    (when (= surveyno (:surveyno uuid-survey-info))
      uuid-survey-info)))

;; save a survey doc
(defn save-survey! [{:keys [survey-table] :as ds} survey-info]
  ;; consider using git as a backend for the doc data.
  ;; queue up and save intermittently if autosave.
  ;; attempt to flush if saved explicitly.
  (let [upserted-survey-info (upsert-survey! ds survey-info)]
    (flush-table survey-table)
    upserted-survey-info))

(defn update-in-survey! [{:keys [survey-table]} [surveyno & _ :as keyvec] update-fn]
  (let [uuid (as-id surveyno)]
    (when (get (view survey-table) uuid)
      (swap! (fs/data survey-table) update-in keyvec update-fn)
      (flush-table survey-table)
      (get (view survey-table) uuid))))

;; use a function because partial needs the table to be mounted first
(defn read-answers [{:keys [answer-table]} surveyno]
  (read-table-entry answer-table surveyno))

(defn read-answer-form [{:keys [answer-table]} surveyno formno]
  (-> (read-table-entry answer-table surveyno)
      (get formno)))

(defn next-answer-counter!
  ([table surveyno formno]
   (or formno (next-answer-counter! table surveyno)))
  ([table surveyno]
   (let [keyvec [:form-counter surveyno]
         result (swap! (fs/data table) update-in keyvec next-counter)
         counter (get-in result keyvec)]
      (counter-str counter))))

(defn update-answers! [{:keys [answer-table]} surveyno {:keys [formno] :as answers}]
  (let [upsert-formno (next-answer-counter! answer-table surveyno formno)
        answers-with-formno (assoc answers :formno upsert-formno)]
    (when upsert-formno
      (swap! (fs/data answer-table) assoc-in [surveyno upsert-formno] answers-with-formno)
      upsert-formno)))

;; TODO decide where to queue write operations
(defn save-answers! [{:keys [answer-table] :as ds} surveyno {:keys [answers]}]
  (when surveyno
    (let [formno (update-answers! ds (as-id surveyno) answers)]
      (flush-table answer-table)
      formno)))

;; Holder of state for store
(defmethod ig/init-key :anketeurweb/ds [_ {:keys [env]}]
  {:survey-table (init-demo-survey! env (read-app-table env "survey-table"))
   :answer-table (read-app-table env "answer-table")})

(defmethod ig/halt-key! :anketeurweb/ds [_ ds]
  (run! flush-table (vals ds)))

