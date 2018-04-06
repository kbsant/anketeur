(ns anketeur.controller.answer
  (:require
    [clojure.tools.logging :as log]
    [anketeur.layout :as layout]
    [anketeur.survey :as survey]
    [anketeur.view.answer :as view.answer]
    [ring.util.http-response :as response]))

(defn render-opener []
  (let [doclist (->> (survey/query-docs nil)
                   vals
                   (into []))
        errors (when (empty? doclist)
                  ["No documents found. Please create a new document."])]
    (layout/render-hiccup
      view.answer/opener
      {:glossary {:title "Respond to a Survey"}
       :flash-errors errors
       :open-link-base "/answer/id/"
       :doclist doclist})))

(defn render-add [surveyno]
  (let [survey-info (survey/read-doc surveyno)]
    (log/info "surveyno: " surveyno "survey-info: " survey-info)
    (layout/render-hiccup
      view.answer/add
      {:survey-info (select-keys survey-info [:surveyno :surveyname :description])
       :flash-errors (when-not survey-info "Error: Unable to open survey for answering.")
       :glossary {:title "Survey"}})))

(defn add-action [{:keys [params] :as request}]
  (let [{:keys [surveyno]} params
        survey-info (when surveyno (survey/read-doc surveyno))
        formno (when survey-info (survey/save-answers! surveyno {:answers {}}))]
    (log/info "surveyno: " surveyno "formno:" formno)
    (if formno
      (-> (response/see-other (str "/answer/id/" surveyno "/formno/" formno)))
      (-> (response/internal-server-error "Error: Unable to open survey for answering.")))))

(defn render-responder [surveyno formno]
  (let [survey-info (survey/read-doc surveyno)
        form (survey/read-answer-form surveyno formno)]
    (log/info "surveyno: " surveyno "form:" form)
    (layout/render-hiccup
      view.answer/responder
      {:survey-info (assoc-in survey-info [:answers :formno] (when form formno))
       :flash-errors (when-not form "Error: Unable to open survey for answering.")
       :glossary {:title "Survey"}})))

;; TODO sanitize/validate form data
(defn answer-action [{:keys [params] :as request}]
  (let [{:keys [surveyno answers]} params
        save-status (survey/save-answers! surveyno {:answers answers})]
    (log/info "save answers: " answers " params: " params " request: " request)
    (if save-status
      (response/ok "Form saved.")
      (response/internal-server-error "Internal error: unable to save form."))))

