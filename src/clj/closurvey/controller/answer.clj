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
     :open-link-base "/answer/id/"
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

;; TODO sanitize/validate form data
(defn answer-action [{:keys [params] :as request}]
  (let [{:keys [surveyno answers]} params
        save-status (survey/save-answers! surveyno {:answers answers})]
    (log/info "save answers: " answers " params: " params " request: " request)
    (if save-status
      (response/ok "Form saved.")
      (response/internal-server-error "Internal error: unable to save form."))))

