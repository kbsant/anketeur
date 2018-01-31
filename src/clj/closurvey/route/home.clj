(ns closurvey.route.home
  (:require 
    [closurvey.controller.edit :as edit]
    [closurvey.controller.main :as main]
    [compojure.core :refer [defroutes GET]]
    [ring.util.http-response :as response]
    [clojure.java.io :as io]))

(defroutes home-routes
  (GET "/" [] (main/render))
  (GET "/edit" [] (edit/render-opener))
  (GET "/edit/:surveyname" [surveyname] (edit/render-editor surveyname))
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
           (response/header "Content-Type" "text/plain; charset=utf-8"))))

