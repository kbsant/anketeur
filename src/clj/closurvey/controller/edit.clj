(ns closurvey.controller.edit
  (:require
    [clojure.tools.logging :as log]
    [closurvey.layout :as layout]
    [closurvey.survey :as survey]
    [closurvey.view.edit :as view.edit]
    [ring.util.http-response :as response]))

;; TODO direct object reference vulnerability - add session mapping and/or auth.

(defn add-action []
  ;; create a new doc, select the new id and redirect to editor
  (let [doc (survey/insert-survey nil nil)
        surveyno (:surveyno doc)]
    (if surveyno
      (-> (response/see-other (str "/edit/id/" surveyno)))
      (-> (response/internal-server-error "Internal error: Unable to add new document.")))))
;; TODO sanitize/validate form data
(defn save-action [{:keys [params] :as request}]
  (let [{:keys [survey-info]} params
        save-status (survey/save-survey! {:survey-info survey-info})]
    (log/info "save survey-info: " survey-info " params: " params " request: " request)
    (if save-status
      (response/ok "Document saved.")
      (response/internal-server-error "Internal error: unable to save document."))))

(defn render-opener []
  (layout/render-hiccup
    view.edit/opener
    {:glossary {:title "Create or Edit a Survey"}
     :add-link "/add"
     :open-link-base "/edit/id/"
     :doclist (->> (survey/query-docs nil)
                   vals
                   (into []))}))

(defn render-editor [surveyno]
  ;; TODO get selected survey from session
  (let [survey-info (survey/read-doc surveyno)]
    (log/info "surveyno: " surveyno "survey-info: " survey-info)
    (layout/render-hiccup
      view.edit/editor
      {:survey-info survey-info
       :glossary {:title "Survey Editor"}})))

