(ns anketeur.client.opener
  (:require
    [clojure.string :as string]
    [anketeur.client.ui :as ui]
    [anketeur.form :as form]
    [reagent.core :as r]))

(defonce state
  (r/atom
    {:doclist []}))

(defn doc-opener [state]
  (fn []
    (form/open-doclist (assoc @state :csrf-token js/csrfToken))))

(defn home-page []
  [:div.container
    [doc-opener state]])

(defn mount-components []
  (r/render [home-page] (.getElementById js/document "app")))

(defn load-transit! []
  (let [init-state (ui/read-json js/transitState)]
    (swap! state merge init-state)))

(defn ^:export init []
  (mount-components)
  (load-transit!))

