(ns closurvey.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[closurvey started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[closurvey has shut down successfully]=-"))
   :middleware identity})
