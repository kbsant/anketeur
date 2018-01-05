(ns user
  (:require [luminus-migrations.core :as migrations]
            [closurvey.config :refer [env]]
            [mount.core :as mount]
            [closurvey.figwheel :refer [start-fw stop-fw cljs]]
            closurvey.core))

(defn start []
  (mount/start-without #'closurvey.core/repl-server))

(defn stop []
  (mount/stop-except #'closurvey.core/repl-server))

(defn restart []
  (stop)
  (start))

(defn migrate []
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback []
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))

(defn create-migration [name]
  (migrations/create name (select-keys env [:database-url])))


