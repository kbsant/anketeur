(ns closurvey.controller.result
  (:require
    [clojure.tools.logging :as log]
    [closurvey.layout :as layout]
    [closurvey.model :as model]
    [closurvey.survey :as survey]
    [closurvey.view.result :as view.result]
    [ring.util.http-response :as response]))

(defn render-opener []
  (let [doclist (->> (survey/query-docs nil)
                   vals
                   (into []))
        errors (when (empty? doclist)
                  ["No documents found. Please create a new document."])]
    (layout/render-hiccup
      view.result/opener
      {:glossary {:title "Survey Results"}
       :flash-errors errors
       :open-link-base "/result/id/"
       :doclist doclist})))

(defn render-result [surveyno]
  (let [survey-info (survey/read-doc surveyno)
        question-list (model/question-list-view survey-info)
        answers (survey/read-answers surveyno)]
    (log/info "surveyno: " surveyno "questions:" question-list "answers: " answers "survey-info: " survey-info)
    (layout/render-hiccup
      view.result/result-page
      {:survey-info survey-info
       :question-list question-list
       :answers answers
       :glossary {:title "Survey Results"}})))

