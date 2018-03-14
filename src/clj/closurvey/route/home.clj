(ns closurvey.route.home
  (:require
    [closurvey.controller.edit :as edit]
    [closurvey.controller.answer :as answer]
    [closurvey.controller.main :as main]
    [compojure.core :refer [defroutes GET POST]]
    [ring.util.http-response :as response]
    [clojure.java.io :as io]))

(defroutes home-routes
  (GET "/" [] (main/render))
  (POST "/add" [] (edit/add-action))
  (POST "/save" request (edit/save-action request))
  (GET "/answer" [] (answer/render-opener))
  (GET "/open" [] (edit/render-opener))
  (GET "/edit/:surveyno" [surveyno] (edit/render-editor surveyno))
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
           (response/header "Content-Type" "text/plain; charset=utf-8"))))

