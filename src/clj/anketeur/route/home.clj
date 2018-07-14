(ns anketeur.route.home
  (:require
    [anketeur.controller.edit :as edit]
    [anketeur.controller.answer :as answer]
    [anketeur.controller.result :as result]
    [anketeur.controller.main :as main]
    [compojure.core :refer [routes GET POST]]
    [ring.util.http-response :as response]
    [clojure.java.io :as io]))

(defn home-routes [env ds]
  (routes
    (GET "/" [] (main/render env))
    (POST "/fileaction" request (edit/file-action ds request))
    (POST "/save" request (edit/save-action ds request))
    (GET "/answer" [] (answer/render-opener ds))
    (GET "/answer/completed/:surveyno" [surveyno] (answer/completed ds surveyno))
    (POST "/answer" request (answer/answer-action ds request))
    (POST "/answernojs" request (answer/answernojs-action ds request))
    (GET "/answer/id/:surveyno" [surveyno] (answer/render-add ds surveyno))
    (POST "/answer/add" request (answer/add-action ds request))
    (GET "/answer/id/:surveyno/formno/:formno"
         [surveyno formno]
         (answer/render-responder ds surveyno formno))
    (GET "/answernojs/id/:surveyno/formno/:formno"
         [surveyno formno]
         (answer/render-responder-nojs ds surveyno formno))
    (GET "/open" [] (edit/render-opener ds))
    (GET "/edit/id/:surveyno" [surveyno] (edit/render-editor ds surveyno))
    (GET "/edit/export/EDN/id/:surveyno" [surveyno] (edit/export ds surveyno))
    (GET "/result" [] (result/render-opener ds))
    (GET "/result/id/:surveyno" [surveyno] (result/render-result ds surveyno))
    (GET "/result/export/:format/id/:surveyno"
         [format surveyno]
         (result/export ds format surveyno))))

