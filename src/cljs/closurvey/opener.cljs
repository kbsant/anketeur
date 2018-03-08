(ns closurvey.opener
  (:require
    [clojure.string :as string]
    [closurvey.client.event :as event]
    [closurvey.client.ui :as ui]
    [reagent.core :as r]))

(defonce state
  (r/atom
    {:doclist []}))

(defn render-selector-row
  [i {:keys [surveyno surveyname]}]
  ^{:key i}
  [:li
    [:a
      {:href (str"/edit/" surveyno)}
      (or surveyname [:span.label.label-default "(no name)"])]])


(defn doc-selector [state]
  (fn []
    (let [doclist (:doclist @state)]
     [:p (str "aaa " doclist)
      (when-not (empty? doclist)
        [:ul
          (map-indexed render-selector-row doclist)])])))

(defn home-page []
  [:div.container
    [:li [:a {:href "/"} "Home"]]
    [:h1 "Survey Editor"]
    [:ul
      [:li "Add a survey"]]
    [:form.inline
      {:method :post
       :action "/add"}
      [ui/anti-forgery-field js/csrfToken]
      [:input
       {:type :hidden
        :name ""}]
      [:input
        {:type :submit
         :value "Create new"}]]
    [:ul
      [:li "Edit a survey"]]
    [doc-selector state]])

(defn mount-components []
  (r/render [home-page] (.getElementById js/document "app")))

(defn load-transit! []
  (let [init-state (ui/read-transit-state js/transitState [:doclist])]
    (swap! state merge init-state)))

(defn init! []
  (mount-components)
  (load-transit!))

