(ns closurvey.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [closurvey.layout :refer [error-page]]
            [closurvey.route.home :refer [home-routes]]
            [compojure.route :as route]
            [closurvey.env :refer [defaults]]
            [mount.core :as mount]
            [closurvey.middleware :as middleware]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    (-> #'home-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))

(defn app [] (middleware/wrap-base #'app-routes))
