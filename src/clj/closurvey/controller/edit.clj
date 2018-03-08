(ns closurvey.controller.edit
  (:require
    [clojure.tools.logging :as log]
    [closurvey.layout :as layout]
    [closurvey.survey :as survey]
    [closurvey.view.edit :as view.edit]
    [ring.util.http-response :as response]))

;; TODO direct object reference vulnerability - add session mapping and/or auth.

;; save a survey doc
(defn save-doc! [{:keys [doc autosave?]}]
  ;; consider using git as a backend for the doc data.
  ;; queue up and save intermittently if autosave.
  ;; attempt to flush if saved explicitly.
  (survey/upsert-survey doc))

;; read a single doc
(defn read-doc [surveyno]
  (-> survey/survey-table
      survey/view
      (get (survey/as-id surveyno))))

;; get a collection of docs
(defn query-docs [query-params]
  (-> survey/survey-table
      survey/view))

(defn add-action []
  ;; create a new doc, select the new id and redirect to editor
  (let [doc (survey/insert-survey nil nil)
        surveyno (:surveyno doc)]
    (if surveyno
      (-> (response/see-other (str "/edit/" surveyno)))
      (-> (response/internal-server-error "Internal error: Unable to add new document.")))))
(defn render-opener []
  (layout/render-hiccup
    view.edit/opener
    {:glossary {:title "Create or Edit a Survey"}
     :doclist (->> (query-docs nil)
                   vals
                   (into []))}))

(defn render-editor [surveyno]
  ;; TODO get selected survey from session
  (let [survey-info (read-doc surveyno)]
    (log/info "surveyno: " surveyno "survey-info: " survey-info)
    (layout/render-hiccup
      view.edit/editor
      {:survey-info survey-info
       :glossary {:title "Survey Editor"}})))


