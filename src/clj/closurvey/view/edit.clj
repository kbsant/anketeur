(ns closurvey.view.edit
  (:require
    [closurvey.view.parts :as parts]
    [hiccup.page :as page]))

(defn opener [data]
  (let [init-state (merge
                     (select-keys data [:flash-errors :doclist])
                     {:headline "Survey Editor"
                      :add-subhead "Create a Survey"
                      :add-link "/add"
                      :open-subhead "Edit a Survey"
                      :open-link-base "/edit/id/"})]
    (parts/spa-appbase data init-state "closurvey.opener.init();")))

(defn editor [{:keys [survey-info flash-errors] :as data}]
  (parts/appbase
    data
    (parts/js-transit-state
      "transitState"
      {:survey-info survey-info :flash-errors flash-errors})
    (list
      (page/include-js "/js/app.js")
      [:script
        {:type "text/javascript"}
        "closurvey.core.init();"])))



