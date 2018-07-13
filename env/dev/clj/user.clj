(ns user
  (:require
     [anketeur.figwheel :refer [start-fw stop-fw cljs]]
     anketeur.core))

(defn start []
  nil #_(mount/start-without #'anketeur.core/repl-server))

(defn stop []
  nil #_(mount/stop-except #'anketeur.core/repl-server))

(defn restart []
  (stop)
  (start))

