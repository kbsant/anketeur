(ns user
  (:require [mount.core :as mount]
            [anketeur.figwheel :refer [start-fw stop-fw cljs]]
            anketeur.core))

(defn start []
  (mount/start-without #'anketeur.core/repl-server))

(defn stop []
  (mount/stop-except #'anketeur.core/repl-server))

(defn restart []
  (stop)
  (start))

