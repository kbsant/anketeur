(ns closurvey.route.home
  (:require
    [closurvey.controller.edit :as edit]
    [closurvey.controller.answer :as answer]
    [closurvey.controller.result :as result]
    [closurvey.controller.main :as main]
    [compojure.core :refer [defroutes GET POST]]
    [ring.util.http-response :as response]
    [clojure.java.io :as io]))

(defroutes home-routes
  (GET "/" [] (main/render))
  (POST "/add" [] (edit/add-action))
  (POST "/save" request (edit/save-action request))
  (GET "/answer" [] (answer/render-opener))
  (POST "/answer" request (answer/answer-action request))
  (GET "/answer/id/:surveyno" [surveyno] (answer/render-add surveyno))
  (POST "/answer/add" request (answer/add-action request))
  (GET "/answer/id/:surveyno/formno/:formno"
       [surveyno formno] (answer/render-responder surveyno formno))
  (GET "/open" [] (edit/render-opener))
  (GET "/edit/id/:surveyno" [surveyno] (edit/render-editor surveyno))
  (GET "/edit/export/EDN/id/:surveyno" [surveyno] (edit/export surveyno))
  (GET "/result" [] (result/render-opener))
  (GET "/result/id/:surveyno" [surveyno] (result/render-result surveyno))
  (GET "/result/id/:surveyno" [surveyno] (result/render-result surveyno))
  (GET "/result/export/:format/id/:surveyno" [format surveyno] (result/export format surveyno)))

