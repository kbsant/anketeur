(ns anketeur.httpd
  (:require [anketeur.handler :as handler]
            [luminus.repl-server :as repl]
            [luminus.http-server :as http]
            [integrant.core :as ig]))

(defmethod ig/init-key :anketeurweb/httpd [_ {:keys [env ds]}]
  (http/start
    (-> env
        (assoc :handler (handler/app env ds))
        (update :io-threads #(or % (* 2 (.availableProcessors (Runtime/getRuntime)))))
        (update :port #(or (-> env :options :port) %)))))

(defmethod ig/halt-key! :anketeurweb/httpd [_ httpd]
  (http/stop httpd))

#_
(defmethod ig/init-key :anketeurweb/nrepl [_ {:keys [env]}]
  (when-let [nrepl-port (env :nrepl-port)]
    (repl/start {:port nrepl-port})))
#_
(defmethod ig/halt-key! :anketeurweb/nrepl [_ nrepl]
    (repl/stop nrepl))

