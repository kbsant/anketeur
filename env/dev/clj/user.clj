(ns user
  (:require
     [anketeur.config :as config]))

(def repl-state (atom nil))

(defn start []
  (when-let [ctx (config/start-deps {})]
    (reset! repl-state ctx)))

(defn stop []
  (when-let [ctx @repl-state]
    (config/stop-deps ctx)
    (reset! repl-state nil)))

(defn restart []
  (stop)
  (start))

