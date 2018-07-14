(ns anketeur.core
  (:require [anketeur.httpd]
            [anketeur.config :as config]
            [anketeur.survey]
            [clojure.tools.cli :as cli]
            [clojure.tools.logging :as log]
            [integrant.core :as ig])
  (:gen-class))

(def cli-options
  "The command line option config, used by parse-opts."
  [["-p" "--port PORT" "Port number"
    :parse-fn #(Integer/parseInt %)]])

(defn stop-app [system]
  (ig/halt! system 
    (log/info "Stopped components:" (keys system)))
  (shutdown-agents))

(defn start-app [options]
  (let [system (ig/init (config/configure options))] 
    (.addShutdownHook (Runtime/getRuntime) (Thread. #(stop-app system)))
    (log/info "Started components:" (keys system))
    system))

(defn -main [& args]
  (start-app (or (cli/parse-opts args cli-options) {})))

