(ns closurvey.controller.edit
  (:require
    [closurvey.layout :as layout]
    [closurvey.view.edit :as view.edit]))

(defn render []
  (layout/render-hiccup view.edit/render {:glossary {:title "Closurvey Editor"}}))
