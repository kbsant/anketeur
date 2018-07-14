(ns anketeur.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [anketeur.layout :refer [error-page]]
            [anketeur.route.home :refer [home-routes]]
            [compojure.route :as route]
            [anketeur.middleware :as middleware]))

(defn app [env ds]
  (middleware/wrap-base
    (routes
      (-> (home-routes env ds)
          (wrap-routes middleware/wrap-csrf)
          (wrap-routes middleware/wrap-formats))
      (route/not-found
        (:body
          (error-page {:status 404
                       :title "page not found"}))))))

