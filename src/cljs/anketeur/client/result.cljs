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

(def answer-template-labels
  {:radio "Single selection"
   :checkbox "Multiple selection"
   :text-area "Freeform"})

(defn render-thead [template]
  [:thead
   {:style {:color "gray"}}
   [:tr
    [:td [:span.label.label-info (get answer-template-labels template)]]]])

(defn render-free-form [{:keys [template coll-answers]}]
  [:div
    [:table.table
      (render-thead template)
      [:tbody
       (->> coll-answers
        (remove string/blank?)
        (map-indexed
          (fn [i answer]
            ^{:key i}
            [:tr
              [:td answer]])))]]])

(defn render-bar-percent [parent-width percent]
  [:div
    {:style
     {:width parent-width
      :height "0.5em"
      :border "solid 1px gray"}}
    [:div
      {:style
       {:width percent
        :height "100%"
        :background-color "darkblue"
        :font-size "1pt"}
       :dangerouslySetInnerHTML
        {:__html "&nbsp;"}}]])

(defn render-bar-chart [{:keys [template answer-keys answer-agg]}]
  (let [agg-total (reduce + 0 (vals answer-agg))]
    [:div
      [:table.table
        (render-thead template)
        [:tbody
          (map
            (fn [key]
              (let [item-agg (get answer-agg key)
                    percent (some-> item-agg (* 100) (/ agg-total) int (str "%"))]
                ^{:key key}
                [:tr
                  [:td (str key)]
                  [:td (str item-agg)]
                  [:td (render-bar-percent "10em" percent)]
                  [:td percent]]))
            answer-keys)]
        [:tfoot
          [:tr
            [:td "Total"]
            [:td agg-total]
            [:td]
            [:td]]]]]))

(defn render-item
  [itemno {:keys [pos question-text template answer-keys answer-agg] :as agg}]
  ^{:key itemno}
  [:div.row.ml-1.pl-1
    [:p
      [:span.mr-1.font-weight-bold (str pos)]
      [:span.mr-1 question-text]]
    (when-not (= :static template)
      (if (empty? answer-keys)
        (render-free-form agg)
        (render-bar-chart agg)))])

(defn home-page []
  (let [{:keys [survey-info export-link-base question-answer-agg answer-count]
         :as state-info} @state
        {:keys [surveyno surveyname description]} survey-info]
    [:div.container
      [:h1 "Survey results"]
      [:p "Export to"
        [:a.ml-1 {:href (str export-link-base "JSON/id/" surveyno)} "JSON"]
        [:a.ml-1 {:href (str export-link-base "EDN/id/" surveyno)} "EDN"]]
      [:h4 surveyname]
      [:p description]
      [:p (str "Number of responses: " answer-count)]
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

