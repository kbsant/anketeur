(ns closurvey.survey
  (:require
    [closurvey.config :refer [env]]
    [closurvey.filestore :as fs]
    [clojure.string :as str]
    [crypto.password.bcrypt :as password]
    [mount.core :refer [defstate]]))

(defn read-app-table [table-name]
  (fs/read-table (:app-data-dir env) table-name))

;; Holder of state for store
(defstate survey-table
  :start (read-app-table "survey-table") 
  :stop (fs/write-table survey-table))

(defn view [table]
  (some-> table :data deref)) 

;; TODO caller should check if survey-info is nil, then retry
(defn insert-survey [surveyname roles]
  (let [surveyno (java.util.UUID/randomUUID)
        survey-info {:surveyname surveyname :surveyno surveyno :roles roles}]
    (swap! (:data survey-table) assoc surveyno survey-info)
    (when (= surveyno (:surveyno survey-info))
      survey-info)))

(defstate auth-table
  :start (read-app-table "auth-table") 
  :stop (fs/write-table auth-table))

(defn insert-auth [surveyname surveyno passwd]
  (when surveyno
    (let [hashkey (password/encrypt passwd)
          auth-info {:surveyname surveyname :surveyno surveyno :hashkey hashkey}]
      (swap! (:data auth-table) assoc surveyname auth-info)
      auth-info)))

(defn auth-survey [{:keys [surveyno hashkey] :as survey-info} password]
  (when (and surveyno (password/check password hashkey))
    (get (view survey-table) surveyno)))

