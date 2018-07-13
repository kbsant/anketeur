(ns anketeur.core
  (:require [anketeur.handler :as handler]
            [luminus.repl-server :as repl]
            [luminus.http-server :as http]
            [anketeur.config]
            [anketeur.survey]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [integrant.core :as ig])
  (:gen-class))

(defn config [args]
  {:anketeurweb/env {:args args}
   :anketeurweb/survey-table {:env (ig/ref :anketeurweb/env)}
   :anketeurweb/answer-table {:env (ig/ref :anketeurweb/env)}
   :anketeurweb/auth-table {:env (ig/ref :anketeurweb/env)}
   :anketeurweb/httpd {:env (ig/ref :anketeurweb/env)
                       :survey-table (ig/ref :anketeurweb/survey-table)
                       :answer-table (ig/ref :anketeurweb/answer-table)}
   #_ #_ :anketeurweb/nrepl {:env (ig/ref :anketeurweb/env)}})

(def cli-options
  [["-p" "--port PORT" "Port number"
    :parse-fn #(Integer/parseInt %)]])

(defmethod ig/init-key :anketeurweb/httpd [_ {:keys [env survey-table answer-table]}]
  (http/start
    (-> env
        (assoc :handler (handler/app env survey-table answer-table))
        (update :io-threads #(or % (* 2 (.availableProcessors (Runtime/getRuntime)))))
        (update :port #(or (-> env :options :port) %)))))

(defmethod ig/halt-key! :anketeurweb/httpd [_ httpd]
  (http/stop httpd))

#_
(mount/defstate ^{:on-reload :noop}
                http-server
                :start
                (http/start
                  (-> env
                      (assoc :handler (handler/app))
                      (update :io-threads #(or % (* 2 (.availableProcessors (Runtime/getRuntime)))))
                      (update :port #(or (-> env :options :port) %))))
                :stop
                (http/stop http-server))

#_
(defmethod ig/init-key :anketeurweb/nrepl [_ {:keys [env]}]
  (when-let [nrepl-port (env :nrepl-port)]
    (repl/start {:port nrepl-port})))
#_
(defmethod ig/halt-key! :anketeurweb/nrepl [_ nrepl]
    (repl/stop nrepl))

#_
(mount/defstate ^{:on-reload :noop}
                repl-server
                :start
                (when-let [nrepl-port (env :nrepl-port)]
                  (repl/start {:port nrepl-port}))
                :stop
                (when repl-server
                  (repl/stop repl-server)))


(defn stop-app [system]
  (ig/halt! system) 
  #_
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (shutdown-agents))

(defn start-app [args]
  (let [system (ig/init (config args))] 
    (.addShutdownHook (Runtime/getRuntime) (Thread. #(stop-app system)))
    system)
  #_
  (doseq [component (-> args
                        (parse-opts cli-options)
                        mount/start-with-args
                        :started)]
    (log/info component "started"))
  #_
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

(defn -main [& args]
  (start-app (or args {})))

