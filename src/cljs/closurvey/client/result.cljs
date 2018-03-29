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

(def answer-template
  {:radio "Single selection"
   :checkbox "Multiple selection"
   :text-area "Freeform"})

(defn render-item
  [pos {:keys [question-text template answer-keys answer-agg coll-answers]}]
  ^{:key pos}
  [:div.row.ml-1.pl-1
    [:p
      [:span.mr-1.font-weight-bold (str (inc pos))]
      question-text]
    [:p (str answer-keys answer-agg coll-answers)]
    [:p [:span.label.label-info (get answer-template template)]]
    (if (empty? answer-keys)
      [:div
        [:p "This questions has free-form answers only, which cannot be aggregated. The answers are:"]
        [:p (str coll-answers)]]
      [:div
        [:p "Aggregated answers:"]
        [:ul
          (map
            (fn [key]
              ^{:key key}
              [:li (str key " : " (get answer-agg key))])
            answer-keys)]])])

(defn home-page []
  [:div.container
    [:ul [:li [:a {:href "/"} "Home"]]]
    [:h1 "Survey results"]
    [:p "Work in progress, dumping raw data"]
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

