(ns user
  (:require [luminus-migrations.core :as migrations]
            [anketeur.config :refer [env]]
            [mount.core :as mount]
            [anketeur.figwheel :refer [start-fw stop-fw cljs]]
            anketeur.core))

(defn start []
  (mount/start-without #'anketeur.core/repl-server))

(defn stop []
  (mount/stop-except #'anketeur.core/repl-server))

(defn restart []
  (stop)
  (start))

(defn migrate []
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback []
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))

(defn create-migration [name]
  (migrations/create name (select-keys env [:database-url])))


