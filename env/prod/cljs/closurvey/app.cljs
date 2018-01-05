(ns closurvey.app
  (:require 
    [closurvey.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(defn ^:export init_edit []
    (core/init!))

