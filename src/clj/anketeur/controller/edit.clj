(ns anketeur.controller.edit
  (:require
    [clojure.tools.logging :as log]
    [anketeur.layout :as layout]
    [anketeur.model :as model]
    [anketeur.survey :as survey]
    [anketeur.util.core :as util]
    [anketeur.view.edit :as view.edit]
    [ring.util.http-response :as response]))

(defn render-opener
  ([]
   (render-opener nil))
  ([view-info]
   (let [doclist (survey/query-docs (complement :deleted?))]
    (layout/render-hiccup
        view.edit/opener
        (merge
          {:glossary {:title "Create or Edit a Survey"}
           :file-link "/fileaction"
           :open-link-base "/edit/id/"
           :doclist doclist}
          view-info)))))

(defn add-action []
  ;; create a new doc, select the new id and redirect to editor
  (let [doc (survey/insert-survey nil nil)
        surveyno (:surveyno doc)]
    (if surveyno
      (response/see-other (str "/edit/id/" surveyno))
      (layout/error-page
        {:status 500 :title "Error" :message "Can't add a new document"}))))

(defn copy-action [survey-info]
  ;; create a new doc, select the new id and redirect to editor
  (let [doc (survey/save-survey! (model/copy-survey-info survey-info))
        surveyno (:surveyno doc)]
    (if surveyno
      (response/see-other (str "/edit/id/" surveyno))
      (render-opener {:flash-errors ["Error: unable to copy the selected item."]}))))

(defn delete-action [{:keys [surveyno] :as survey-info}]
  ;; create a new doc, select the new id and redirect to editor
  (let [result (survey/update-in-survey! [surveyno] #(assoc % :deleted? true))]
    (if surveyno
      (render-opener {:flash-errors ["Warning: can't undo ."]})
      (render-opener {:flash-errors ["Error: unable to delete the selected item."]}))))

(defn validate-file-action [target-fn orig-surveyno]
  (let [survey-info (when orig-surveyno (survey/read-doc orig-surveyno))]
    (if survey-info
      (target-fn survey-info)
      (render-opener {:flash-errors ["Please select an item."]}))))

;; TODO direct object reference vulnerability - add session mapping and/or auth.
(defn file-action [{:keys [params] :as request}]
  (let [{:keys [fileaction surveyno]} params]
    (log/info "file-action action:" fileaction " surveyno:" surveyno)
    (condp = fileaction
      "new" (add-action)
      "copy" (validate-file-action copy-action surveyno)
      "delete"(validate-file-action delete-action surveyno)
      (layout/error-page {:status 500 :title "Error" :message "Unknown request"}))))

;; TODO sanitize/validate form data
(defn save-action [{:keys [params] :as request}]
  (let [{:keys [survey-info]} params
        save-status (survey/save-survey! {:survey-info survey-info})]
    (log/info "save survey-info: " survey-info " params: " params " request: " request)
    (if save-status
      (response/ok "Document saved.")
      (response/internal-server-error "Internal error: unable to save document."))))

(defn render-editor [surveyno]
  ;; TODO get selected survey from session
  (let [survey-info (survey/read-doc surveyno)]
    (log/info "surveyno: " surveyno "survey-info: " survey-info)
    (layout/render-hiccup
      view.edit/editor
      {:survey-info survey-info
       :export-link-base "/edit/export/"
       :response-link-base "/answer/id/"
       :glossary {:title "Survey Editor"}})))

(defn export [surveyno]
  (let [survey-info (survey/read-doc surveyno)
        text (util/export-format "EDN" survey-info)]
    (layout/render-text text)))
