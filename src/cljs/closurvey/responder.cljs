(ns closurvey.responder
  (:require
    [clojure.string :as string]
    [closurvey.client.event :as event]
    [closurvey.client.surveyform :as form]
    [closurvey.client.ui :as ui]
    [reagent.core :as r]))

(defonce state
  (r/atom {}))

(defn question-list [state]
  (fn []
    (let [questions (form/question-list-view @state)
          render-question (partial form/preview-question @state)]
      [:div.container
        [:div.row
          [:span.font-weight-bold (str "Question List (" (count questions) ")")]]
        (when-not (empty? questions)
          (map-indexed render-question questions))])))

(defn home-page []
  [:div.container
    [:ul [:li [:a {:href "/"} "Home"]]]
    [question-list state]])

(defn mount-components []
  (r/render [home-page] (.getElementById js/document "app")))

(defn load-transit! []
  (let [init-state (ui/read-transit-state js/transitState)
        survey-info (:survey-info init-state)]
    (when survey-info
      (swap! state merge survey-info))))

(defn ^:export init []
  (mount-components)
  (load-transit!))

