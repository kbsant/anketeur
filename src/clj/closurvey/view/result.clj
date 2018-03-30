(ns closurvey.view.result
  (:require
    [closurvey.view.parts :as parts]
    [hiccup.page :as page]))

(defn opener [data]
  (let [init-state (merge
                     (select-keys data [:glossary :flash-errors :doclist :open-link-base])
                     {:headline "Survey Results"
                      :open-subhead "Open"})]
    (parts/spa-appbase data init-state "closurvey.opener.init();")))

(defn result-page [{:keys [survey-info flash-errors] :as data}]
  (parts/appbase
    data
    (parts/js-transit-state "transitState" data)
    (list
      (page/include-js "/js/app.js")
      [:script
        {:type "text/javascript"}
        "closurvey.client.result.init();"])))

