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

(defn home-page []
  [:div.container
    [:ul [:li [:a {:href "/"} "Home"]]]
    [:p "Survey results -- Work in progress, dumping raw data"]
    [:p (str @state)]])

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

