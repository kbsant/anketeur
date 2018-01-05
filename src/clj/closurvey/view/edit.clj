(ns closurvey.view.edit
  (:require
    [closurvey.view.parts :as parts]
    [hiccup.page :as page]))

(defn render [{:keys [characters username flash-errors] :as data}]
  (parts/appbase
    data
    (parts/js-transit-state 
      "transitState"
      {:display-name username :characters characters :flash-errors flash-errors})
    (list
      (page/include-js "/js/app.js") 
      [:script 
        {:type "text/javascript"}
        "closurvey.app.init_edit();"])))



