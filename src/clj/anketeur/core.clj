(ns anketeur.core
  (:require [anketeur.config :as config]
            [anketeur.handler]
            [anketeur.survey]
            [ashikasoft.webstack.core :as webstack]
            [ashikasoft.webstack.httpd]
            [clojure.tools.cli :as cli]
            [clojure.tools.logging :as log]
            [integrant.core :as ig])
  (:gen-class))

(defn stop-deps [system]
  (ig/halt! system)
  (log/info "Stopped components:" (keys system)))

(defn start-deps [options]
  (let [system (ig/init (config/configure options))]
    (log/info "Started components:" (keys system))
    system))

(defn stop-app [system]
  (stop-deps system)
  (shutdown-agents))

(defn start-app [options]
  (let [system (start-deps options)]
    (.addShutdownHook (Runtime/getRuntime) (Thread. #(stop-app system)))
    system))

(defn -main [& args]
  (start-app (webstack/cli-options args)))

