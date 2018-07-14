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
  ([ds]
   (render-opener ds nil))
  ([ds view-info]
   (let [doclist (survey/query-docs ds (complement :deleted?))]
    (layout/render-hiccup
        view.edit/opener
        (merge
          {:glossary {:title "Create or Edit a Survey"}
           :file-link "/fileaction"
           :open-link-base "/edit/id/"
           :doclist doclist}
          view-info)))))

(defn add-action [ds]
  ;; create a new doc, select the new id and redirect to editor
  (let [doc (survey/insert-survey! ds nil nil)
        surveyno (:surveyno doc)]
    (if surveyno
      (response/see-other (str "/edit/id/" surveyno))
      (layout/error-page
        {:status 500 :title "Error" :message "Can't add a new document"}))))

(defn copy-action [ds survey-info]
  ;; create a new doc, select the new id and redirect to editor
  (let [doc (survey/save-survey! ds (model/copy-survey-info survey-info))
        surveyno (:surveyno doc)]
    (if surveyno
      (response/see-other (str "/edit/id/" surveyno))
      (render-opener ds {:flash-errors ["Error: unable to copy the selected item."]}))))

(defn delete-action [ds {:keys [surveyno] :as survey-info}]
  ;; create a new doc, select the new id and redirect to editor
  (let [result (survey/update-in-survey! ds [surveyno] #(assoc % :deleted? true))]
    (if surveyno
      (render-opener ds {:flash-errors ["Warning: can't undo ."]})
      (render-opener ds {:flash-errors ["Error: unable to delete the selected item."]}))))

(defn validate-file-action [ds target-fn orig-surveyno]
  (let [survey-info (when orig-surveyno (survey/read-doc ds orig-surveyno))]
    (if survey-info
      (target-fn ds survey-info)
      (render-opener ds {:flash-errors ["Please select an item."]}))))

;; TODO direct object reference vulnerability - add session mapping and/or auth.
(defn file-action [ds {:keys [params] :as request}]
  (let [{:keys [fileaction surveyno]} params]
    (log/info "file-action action:" fileaction " surveyno:" surveyno)
    (condp = fileaction
      "new" (add-action ds)
      "copy" (validate-file-action ds copy-action surveyno)
      "delete"(validate-file-action ds delete-action surveyno)
      (layout/error-page {:status 500 :title "Error" :message "Unknown request"}))))

;; TODO sanitize/validate form data
(defn save-action [ds {:keys [params] :as request}]
  (let [{:keys [survey-info]} params
        save-status (survey/save-survey! ds survey-info)]
    (log/info "save survey-info: " survey-info " params: " params " request: " request)
    (if save-status
      (response/ok "Document saved.")
      (response/internal-server-error "Internal error: unable to save document."))))

(defn render-editor [ds surveyno]
  ;; TODO get selected survey from session
  (let [survey-info (survey/read-doc ds surveyno)]
    (log/info "surveyno: " surveyno "survey-info: " survey-info)
    (layout/render-hiccup
      view.edit/editor
      {:survey-info survey-info
       :export-link-base "/edit/export/"
       :response-link-base "/answer/id/"
       :glossary {:title "Survey Editor"}})))

(defn export [ds surveyno]
  (let [survey-info (survey/read-doc ds surveyno)
        text (util/export-format "EDN" survey-info)]
    (layout/render-text text)))
