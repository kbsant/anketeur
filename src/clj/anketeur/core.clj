(ns anketeur.core
  (:require [anketeur.config :as config]
            [ashikasoft.webstack.cli :as cli]
            [integrant.core :as ig])
  (:gen-class))

(defn stop-app [system]
  (config/stop-deps system)
  (shutdown-agents))

(defn start-app [options]
  (let [system (config/start-deps options)]
    (cli/add-shutdown-hook #(stop-app system))
    system))

(defn -main [& args]
  (start-app (cli/parse-options args)))

