(ns closurvey.controller.answer
  (:require
    [clojure.tools.logging :as log]
    [closurvey.layout :as layout]
    [closurvey.survey :as survey]
    [closurvey.view.answer :as view.answer]
    [ring.util.http-response :as response]))

(defn render-opener []
  (layout/render-hiccup
    view.answer/opener
    {:glossary {:title "Respond to a Survey"}
     :doclist (->> (survey/query-docs nil)
                   vals
                   (into []))}))


