(ns anketeur.handler
  (:require [anketeur.env :refer [defaults]]
            [anketeur.route.home :refer [home-routes]]
            [ashikasoft.webstack.handler :as webstack.handler]
            [ashikasoft.webstack.middleware :as webstack.middleware]
            [reitit.ring :as ring]
            [integrant.core :as ig]))

(defn app [env ds]
  (webstack.middleware/wrap-base
    ((:middleware defaults)
     (ring/ring-handler
      (ring/router
        [(home-routes env ds)])
      (webstack.handler/common-routes)))))

(defmethod ig/init-key :anketeurweb/handler [_ {:keys [env ds]}]
  (app env ds))

