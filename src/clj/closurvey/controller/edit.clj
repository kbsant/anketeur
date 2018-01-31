(ns closurvey.controller.edit
  (:require
    [closurvey.layout :as layout]
    [closurvey.view.edit :as view.edit]))

(defn render-opener []
  (layout/render-hiccup
    view.edit/opener 
    {:glossary {:title "Create or Edit a Survey"}}))

(defn render-editor [surveyname]
  (layout/render-hiccup
    view.edit/editor
    {:surveyname surveyname
     :glossary {:title "Survey Editor"}}))
