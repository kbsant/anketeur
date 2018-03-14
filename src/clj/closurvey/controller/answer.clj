(ns closurvey.controller.answer
  (:require
    [clojure.tools.logging :as log]
    [closurvey.layout :as layout]
    [closurvey.survey :as survey]
    [closurvey.view.answer :as view.answer]
    [ring.util.http-response :as response]))

(defn render-opener []
  (layout/render-hiccup
    view.answer/opener
    {:glossary {:title "Respond to a Survey"}
     :doclist (->> (survey/query-docs nil)
                   vals
                   (into []))}))

(defn render-responder [surveyno]
  (let [survey-info (survey/read-doc surveyno)]
    (log/info "surveyno: " surveyno "survey-info: " survey-info)
    (layout/render-hiccup
      view.answer/responder
      {:survey-info survey-info
       :glossary {:title "Survey"}})))

