(ns anketeur.client.result
  (:require
    [clojure.string :as string]
    [anketeur.form :as form]
    [anketeur.client.ui :as ui]
    [reagent.core :as r]
    [anketeur.client.ajax :as appajax]
    [ajax.core :refer [GET POST]]))

(defonce state
  (r/atom {}))

(def answer-template
  {:radio "Single selection"
   :checkbox "Multiple selection"
   :text-area "Freeform"})

(defn render-item
  [itemno {:keys [pos question-text template answer-keys answer-agg coll-answers]}]
  ^{:key itemno}
  [:div.row.ml-1.pl-1
    [:p
      [:span.mr-1.font-weight-bold (str pos)]
      [:span.mr-1 question-text]
      [:span.label.label-info (get answer-template template)]]
    (when-not (= :static template)
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
              answer-keys)]]))])

(defn home-page []
  (let [{:keys [survey-info export-link-base question-answer-agg] :as state-info} @state
                surveyno (:surveyno survey-info)]
    [:div.container
      [:h1 "Survey results"]
      [:p "Export to"
        [:a.ml-1 {:href (str export-link-base "JSON/id/" surveyno)} "JSON"]
        [:a.ml-1 {:href (str export-link-base "EDN/id/" surveyno)} "EDN"]]
      (map-indexed render-item question-answer-agg)]))

(defn mount-components []
  (r/render [home-page] (.getElementById js/document "app")))

(defn load-transit! []
  (let [init-state (ui/read-json js/transitState)]
    (when init-state
      (reset! state init-state))))

(defn ^:export init []
  (appajax/load-interceptors! js/context js/csrfToken)
  (mount-components)
  (load-transit!))

