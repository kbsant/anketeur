(ns closurvey.controller.result
  (:require
    [clojure.tools.logging :as log]
    [closurvey.layout :as layout]
    [closurvey.survey :as survey]
    [closurvey.view.result :as view.result]
    [ring.util.http-response :as response]))

(defn render-opener []
  (layout/render-hiccup
    view.result/opener
    {:glossary {:title "Survey Results"}
     :open-link-base "/result/id/"
     :doclist (->> (survey/query-docs nil)
                   vals
                   (into []))}))

(defn render-result [surveyno]
  (let [survey-info (survey/read-doc surveyno)
        question-list (:question-list survey-info)
        answers (survey/read-answers surveyno)]
    (log/info "surveyno: " surveyno "answers: " answers "survey-info: " survey-info)
    (layout/render-hiccup
      view.result/result-page
      {:survey-info survey-info
       :question-list question-list
       :answers answers
       :glossary {:title "Survey Results"}})))

