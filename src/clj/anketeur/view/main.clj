(ns anketeur.view.main
  (:require
    [anketeur.style :as style :refer [wide-space]]
    [anketeur.view.parts :as parts]))

(defn content [{:keys [appname message]}]
  [:div.container
    (style/navbar [:span.label.label-info {:style "background : black; padding: 5px; border-radius: 1px;"} "ア"])
    [:h1 appname]
    (when message [:p message])
    [:ul
      [:li [:a {:href "/open"} "Create or edit a survey"]]
      [:li [:a {:href "/answer"} "Respond to a survey"]]
      [:li [:a {:href "/result"} "Gather results"]]]])

(defn render [{:keys [glossary]}]
  (parts/main glossary 
    nil
    (content glossary)))

