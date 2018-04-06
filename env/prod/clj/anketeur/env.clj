(ns anketeur.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[anketeur started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[anketeur has shut down successfully]=-"))
   :middleware identity})
