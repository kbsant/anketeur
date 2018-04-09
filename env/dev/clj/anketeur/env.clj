(ns anketeur.env
  (:require [clojure.tools.logging :as log]
            [anketeur.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[anketeur started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[anketeur has shut down successfully]=-"))
   :middleware wrap-dev})
