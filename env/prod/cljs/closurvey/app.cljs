(ns closurvey.app
  (:require 
    [closurvey.core :as core]
    [closurvey.opener :as opener]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(defn ^:export init_opener []
    (opener/init!))

(defn ^:export init_edit []
    (core/init!))

