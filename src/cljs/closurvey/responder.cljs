(ns closurvey.responder
  (:require
    [clojure.string :as string]
    [closurvey.client.event :as event]
    [closurvey.client.ui :as ui]
    [reagent.core :as r]))

(defonce state
  (r/atom {}))

(defn home-page []
  [:div.container
    [:li [:a {:href "/"} "Home"]]
    [doc-opener state]])

(defn mount-components []
  (r/render [home-page] (.getElementById js/document "app")))

(defn load-transit! []
  (let [init-state (ui/read-transit-state js/transitState)]
    (swap! state merge init-state)))

(defn ^:export init []
  (mount-components)
  (load-transit!))

