(ns closurvey.view.answer
  (:require
    [closurvey.view.parts :as parts]
    [hiccup.page :as page]))

(defn opener [data]
  (let [init-state (merge
                     (select-keys data [:flash-errors :doclist])
                     {:headline "Respond to a Survey"
                      :open-subhead "Open"
                      :open-link-base "/answer/id/"})]
    (parts/spa-appbase data init-state "closurvey.opener.init();")))

(defn responder [{:keys [survey-info flash-errors] :as data}]
  (parts/appbase
    data
    (parts/js-transit-state
      "transitState"
      {:survey-info survey-info :flash-errors flash-errors})
    (list
      (page/include-js "/js/app.js")
      [:script
        {:type "text/javascript"}
        "closurvey.responder.init();"])))


