(ns closurvey.core
  (:require [reagent.core :as r]))

(defn home-page []
  [:div.container
    [:h1 "Closurvey Editor"]
    [:ul
      [:li "Add a survey"]
      [:li "Edit a survey"]]])

;; -------------------------
;; Initialize app

(defn mount-components []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-components))
