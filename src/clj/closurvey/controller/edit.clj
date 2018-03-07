(ns closurvey.controller.edit
  (:require
    [clojure.tools.logging :as log]
    [closurvey.layout :as layout]
    [closurvey.survey :as survey]
    [closurvey.view.edit :as view.edit]
    [ring.util.http-response :as response]))

(defn add-action []
  ;; create a new doc, select the new id and redirect to editor
  (let [doc (survey/insert-survey nil nil)
        surveyno (:surveyno doc)]
    (-> (response/see-other "/edit")
        (assoc-in [:session :surveyno] surveyno))))

(defn render-opener []
  (layout/render-hiccup
    view.edit/opener
    {:glossary {:title "Create or Edit a Survey"}
     :doclist (->> survey/survey-table
                   survey/view
                   vals
                   (into []))}))

(defn render-editor [{:keys [session] :as request}]
  ;; TODO get selected survey from session
  (let [surveyname nil]
    (log/info "session: " session)
    (layout/render-hiccup
      view.edit/editor
      {:surveyname surveyname
       :glossary {:title "Survey Editor"}})))


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
      (get surveyno)))

;; get a collection of docs
(defn query-docs [query-params]
  (-> survey/survey-table
      survey/view))
