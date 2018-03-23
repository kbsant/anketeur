(ns closurvey.controller.main
  (:require
    [closurvey.config :refer [env]]
    [closurvey.layout :as layout]
    [closurvey.view.main :as view.main]))

(defn render []
  (layout/render-hiccup view.main/render {:glossary {:title "Survey"} :message (:app-message env)}))
