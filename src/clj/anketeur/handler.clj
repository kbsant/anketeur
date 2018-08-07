(ns anketeur.handler
  (:require [anketeur.env :refer [defaults]]
            [anketeur.route.home :refer [home-routes]]
            [ashikasoft.webstack.handler :as webstack.handler]
            [ashikasoft.webstack.middleware :as webstack.middleware]
            [reitit.ring :as ring]))

(defn app [env ds]
  (webstack.middleware/wrap-base
    ((:middleware defaults)
     (ring/ring-handler
      (ring/router
        [(home-routes env ds)])
      (webstack.handler/common-routes)))))
