(ns anketeur.route.home
  (:require
    [anketeur.controller.edit :as edit]
    [anketeur.controller.answer :as answer]
    [anketeur.controller.result :as result]
    [anketeur.controller.main :as main]
    [compojure.core :refer [routes GET POST]]
    [ring.util.http-response :as response]
    [clojure.java.io :as io]))

(defn home-routes [env survey-table answer-table]
  (routes
    (GET "/" [] (main/render env))
    (POST "/fileaction" request (edit/file-action survey-table request))
    (POST "/save" request (edit/save-action survey-table request))
    (GET "/answer" [] (answer/render-opener survey-table))
    (GET "/answer/completed/:surveyno" [surveyno] (answer/completed survey-table surveyno))
    (POST "/answer" request (answer/answer-action answer-table request))
    (POST "/answernojs" request (answer/answernojs-action answer-table request))
    (GET "/answer/id/:surveyno" [surveyno] (answer/render-add survey-table surveyno))
    (POST "/answer/add" request (answer/add-action survey-table answer-table request))
    (GET "/answer/id/:surveyno/formno/:formno"
         [surveyno formno]
         (answer/render-responder survey-table answer-table surveyno formno))
    (GET "/answernojs/id/:surveyno/formno/:formno"
         [surveyno formno]
         (answer/render-responder-nojs survey-table answer-table surveyno formno))
    (GET "/open" [] (edit/render-opener survey-table))
    (GET "/edit/id/:surveyno" [surveyno] (edit/render-editor survey-table surveyno))
    (GET "/edit/export/EDN/id/:surveyno" [surveyno] (edit/export survey-table surveyno))
    (GET "/result" [] (result/render-opener survey-table))
    (GET "/result/id/:surveyno" [surveyno] (result/render-result survey-table answer-table surveyno))
    (GET "/result/export/:format/id/:surveyno"
         [format surveyno]
         (result/export survey-table answer-table format surveyno))))

