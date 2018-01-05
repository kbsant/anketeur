(ns closurvey.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [closurvey.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[closurvey started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[closurvey has shut down successfully]=-"))
   :middleware wrap-dev})
