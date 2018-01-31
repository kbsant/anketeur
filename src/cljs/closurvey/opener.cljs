(ns closurvey.opener
  (:require
    [clojure.string :as string]
    [closurvey.client.event :as event] 
    [reagent.core :as r]))

(defonce state (r/atom {}))

(defn home-page []
  [:div.container
    [:li [:a {:href "/"} "Home"]]
    [:h1 "Survey Editor"]
    [:ul
      [:li "Add a survey"]
      [:li "Edit a survey"]]])
 
(defn mount-components []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-components))

