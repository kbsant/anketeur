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
  ([survey-table]
   (render-opener survey-table nil))
  ([survey-table view-info]
   (let [doclist (survey/query-docs survey-table (complement :deleted?))]
    (layout/render-hiccup
        view.edit/opener
        (merge
          {:glossary {:title "Create or Edit a Survey"}
           :file-link "/fileaction"
           :open-link-base "/edit/id/"
           :doclist doclist}
          view-info)))))

(defn add-action [survey-table]
  ;; create a new doc, select the new id and redirect to editor
  (let [doc (survey/insert-survey survey-table nil nil)
        surveyno (:surveyno doc)]
    (if surveyno
      (response/see-other (str "/edit/id/" surveyno))
      (layout/error-page
        {:status 500 :title "Error" :message "Can't add a new document"}))))

(defn copy-action [survey-table survey-info]
  ;; create a new doc, select the new id and redirect to editor
  (let [doc (survey/save-survey! survey-table (model/copy-survey-info survey-info))
        surveyno (:surveyno doc)]
    (if surveyno
      (response/see-other (str "/edit/id/" surveyno))
      (render-opener survey-table {:flash-errors ["Error: unable to copy the selected item."]}))))

(defn delete-action [survey-table {:keys [surveyno] :as survey-info}]
  ;; create a new doc, select the new id and redirect to editor
  (let [result (survey/update-in-survey! survey-table [surveyno] #(assoc % :deleted? true))]
    (if surveyno
      (render-opener survey-table {:flash-errors ["Warning: can't undo ."]})
      (render-opener survey-table {:flash-errors ["Error: unable to delete the selected item."]}))))

(defn validate-file-action [survey-table target-fn orig-surveyno]
  (let [survey-info (when orig-surveyno (survey/read-doc survey-table orig-surveyno))]
    (if survey-info
      (target-fn survey-table survey-info)
      (render-opener {:flash-errors ["Please select an item."]}))))

;; TODO direct object reference vulnerability - add session mapping and/or auth.
(defn file-action [survey-table {:keys [params] :as request}]
  (let [{:keys [fileaction surveyno]} params]
    (log/info "file-action action:" fileaction " surveyno:" surveyno)
    (condp = fileaction
      "new" (add-action survey-table)
      "copy" (validate-file-action survey-table copy-action surveyno)
      "delete"(validate-file-action survey-table delete-action surveyno)
      (layout/error-page {:status 500 :title "Error" :message "Unknown request"}))))

;; TODO sanitize/validate form data
(defn save-action [survey-table {:keys [params] :as request}]
  (let [{:keys [survey-info]} params
        save-status (survey/save-survey! survey-table survey-info)]
    (log/info "save survey-info: " survey-info " params: " params " request: " request)
    (if save-status
      (response/ok "Document saved.")
      (response/internal-server-error "Internal error: unable to save document."))))

(defn render-editor [survey-table surveyno]
  ;; TODO get selected survey from session
  (let [survey-info (survey/read-doc survey-table surveyno)]
    (log/info "surveyno: " surveyno "survey-info: " survey-info)
    (layout/render-hiccup
      view.edit/editor
      {:survey-info survey-info
       :export-link-base "/edit/export/"
       :response-link-base "/answer/id/"
       :glossary {:title "Survey Editor"}})))

(defn export [survey-table surveyno]
  (let [survey-info (survey/read-doc survey-table surveyno)
        text (util/export-format "EDN" survey-info)]
    (layout/render-text text)))
