(ns user
  (:require
     [anketeur.figwheel :refer [start-fw stop-fw cljs]]
     [anketeur.core :as core]))

(def repl-state (atom nil))

(defn start []
  (when-let [ctx (core/start-deps {})]
    (reset! repl-state ctx)))

(defn stop []
  (when-let [ctx @repl-state]
    (core/stop-deps ctx)
    (reset! repl-state nil)))

(defn restart []
  (stop)
  (start))

