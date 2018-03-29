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
        answer-types (:answer-types survey-info)
        answers (survey/read-answers surveyno)
        question-answer-agg (->> question-list
                                (model/questions-with-answer-keys answer-types)
                                (model/questions-with-coll-answers answers)
                                (model/questions-with-agg-answers))]
    (log/info "surveyno: " surveyno "qa-agg:" question-answer-agg)
    (layout/render-hiccup
      view.result/result-page
      {:survey-info (select-keys survey-info [:surveyno :surveyname])
       :question-answer-agg question-answer-agg
       :glossary {:title "Survey Results"}})))

