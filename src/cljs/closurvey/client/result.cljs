(ns closurvey.client.result
  (:require
    [clojure.string :as string]
    [closurvey.client.event :as event]
    [closurvey.client.surveyform :as form]
    [closurvey.client.ui :as ui]
    [reagent.core :as r]
    [closurvey.ajax :as appajax]
    [ajax.core :refer [GET POST]]))

(defonce state
  (r/atom {}))

(defn render-item [index {:keys [question-text answer-keys answer-agg coll-answers]}]
  ^{:key index}
  [:div.row.ml-1.pl-1
    [:p question-text]
    [:p (str answer-keys answer-agg coll-answers)]])

(defn home-page []
  [:div.container
    [:ul [:li [:a {:href "/"} "Home"]]]
    [:p "Survey results -- Work in progress, dumping raw data"]
    [:p (str @state)]
    (map-indexed render-item (:question-answer-agg @state))])

(defn mount-components []
  (r/render [home-page] (.getElementById js/document "app")))

(defn load-transit! []
  (let [init-state (ui/read-transit-state js/transitState)]
    (when init-state
      (reset! state init-state))))

(defn ^:export init []
  (appajax/load-interceptors! js/context js/csrfToken)
  (mount-components)
  (load-transit!))

