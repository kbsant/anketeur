(ns closurvey.view.answer
  (:require
    [closurvey.view.parts :as parts]
    [ring.util.anti-forgery :refer [anti-forgery-field]]
    [hiccup.page :as page]))

(defn add-content [{:keys [surveyname surveyno description] :as survey-info}]
  [:div.container
    [:h1 "Respond to a survey"]
    [:h2 surveyname]
    [:p description]
    [:form {:method :POST :action "/answer/add"}
      (anti-forgery-field)
      [:input {:type :hidden :name "surveyno" :value surveyno}]
      [:input {:type :submit :value "Start"}]]])

(defn add [{:keys [survey-info glossary message]}]
  (parts/main
    glossary
    nil
    (add-content survey-info)))

(defn opener [data]
  (let [init-state (merge
                     (select-keys data [:glossary :flash-errors :doclist :open-link-base])
                     {:headline "Respond to a Survey"
                      :open-subhead "Open"})]
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

