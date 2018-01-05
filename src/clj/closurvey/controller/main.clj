(ns closurvey.controller.main
  (:require
    [closurvey.layout :as layout]
    [closurvey.view.main :as view.main]))

(defn render []
  (layout/render-hiccup view.main/render {:glossary {:title "Closurvey"}}))
