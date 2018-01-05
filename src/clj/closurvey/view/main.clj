(ns closurvey.view.main
  (:require
    [closurvey.view.parts :as parts]))

(defn content []
  [:div.container
    [:h1 "Closurvey"]
    [:ul
      [:li [:a {:href "/answer"} "Respond to a survey"]]
      [:li [:a {:href "/edit"} "Create or edit a survey"]]
      [:li [:a {:href "/result"} "Gather results"]]]])

(defn render [{:keys [glossary]}]
  (parts/main glossary 
    nil
    (content)))

