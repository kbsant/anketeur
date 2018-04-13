(ns anketeur.route.home
  (:require
    [anketeur.controller.edit :as edit]
    [anketeur.controller.answer :as answer]
    [anketeur.controller.result :as result]
    [anketeur.controller.main :as main]
    [compojure.core :refer [defroutes GET POST]]
    [ring.util.http-response :as response]
    [clojure.java.io :as io]))

(defroutes home-routes
  (GET "/" [] (main/render))
  (POST "/add" [] (edit/add-action))
  (POST "/save" request (edit/save-action request))
  (GET "/answer" [] (answer/render-opener))
  (GET "/answer/completed/:surveyno" [surveyno] (answer/completed surveyno))
  (POST "/answer" request (answer/answer-action request))
  (POST "/answernojs" request (answer/answernojs-action request))
  (GET "/answer/id/:surveyno" [surveyno] (answer/render-add surveyno))
  (POST "/answer/add" request (answer/add-action request))
  (GET "/answer/id/:surveyno/formno/:formno"
       [surveyno formno] (answer/render-responder surveyno formno))
  (GET "/answernojs/id/:surveyno/formno/:formno"
       [surveyno formno] (answer/render-responder-nojs surveyno formno))
  (GET "/open" [] (edit/render-opener))
  (GET "/edit/id/:surveyno" [surveyno] (edit/render-editor surveyno))
  (GET "/edit/export/EDN/id/:surveyno" [surveyno] (edit/export surveyno))
  (GET "/result" [] (result/render-opener))
  (GET "/result/id/:surveyno" [surveyno] (result/render-result surveyno))
  (GET "/result/id/:surveyno" [surveyno] (result/render-result surveyno))
  (GET "/result/export/:format/id/:surveyno" [format surveyno] (result/export format surveyno)))

