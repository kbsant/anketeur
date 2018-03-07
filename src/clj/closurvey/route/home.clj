(ns closurvey.route.home
  (:require 
    [closurvey.controller.edit :as edit]
    [closurvey.controller.main :as main]
    [compojure.core :refer [defroutes GET POST]]
    [ring.util.http-response :as response]
    [clojure.java.io :as io]))

(defroutes home-routes
  (GET "/" [] (main/render))
  (POST "/add" [] (edit/add-action))
  (GET "/open" [] (edit/render-opener))
  (GET "/edit" request (edit/render-editor request))
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
           (response/header "Content-Type" "text/plain; charset=utf-8"))))

