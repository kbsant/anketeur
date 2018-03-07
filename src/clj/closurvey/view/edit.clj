(ns closurvey.view.edit
  (:require
    [closurvey.view.parts :as parts]
    [hiccup.page :as page]))

(defn opener [{:keys [flash-errors doclist] :as data}]
  (parts/appbase
    data
    (parts/js-transit-state 
      "transitState"
      {:flash-errors flash-errors
       :doclist doclist})
    (list
      (page/include-js "/js/app.js") 
      [:script 
        {:type "text/javascript"}
        "closurvey.app.init_opener();"])))

(defn editor [{:keys [surveyname flash-errors] :as data}]
  (parts/appbase
    data
    (parts/js-transit-state 
      "transitState"
      {:surveyname surveyname :flash-errors flash-errors})
    (list
      (page/include-js "/js/app.js") 
      [:script 
        {:type "text/javascript"}
        "closurvey.app.init_edit();"])))



