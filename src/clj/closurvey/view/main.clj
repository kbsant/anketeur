(ns closurvey.view.main
  (:require
    [closurvey.view.parts :as parts]))

(defn content [message]
  [:div.container
    [:h1 "Closurvey"]
    (when message [:p message])
    [:ul
      [:li [:a {:href "/answer"} "Respond to a survey"]]
      [:li [:a {:href "/open"} "Create or edit a survey"]]
      [:li [:a {:href "/result"} "Gather results"]]]])

(defn render [{:keys [glossary message]}]
  (parts/main glossary 
    nil
    (content message)))

