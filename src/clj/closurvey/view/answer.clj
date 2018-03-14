(ns closurvey.view.answer
  (:require
    [closurvey.view.parts :as parts]
    [hiccup.page :as page]))

(defn opener [data]
  (let [init-state (merge
                     (select-keys data [:flash-errors :doclist])
                     {:headline "Respond to a Survey"
                      :open-subhead "Open"
                      :open-link-base "/open/"})]
    (parts/spa-appbase data init-state "closurvey.app.init_opener();")))

