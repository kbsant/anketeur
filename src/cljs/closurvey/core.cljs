(ns closurvey.core
  (:require [reagent.core :as r]
            [ajax.core :refer [GET POST]]))

(defonce state 
  (r/atom
    {:surveyname ""
     :passphrase ""
     :current-question ""
     :current-required ""
     :current-allow-na ""})) 


(defn name-and-passphrase-opener [state]
  (fn []
    [:div.container
      [:div.row [:span.badge.badge-info "Create or edit a survey"]]
      [:div.row
        [:input {:type :text :value (:surveyname @state) :placeholder "Survey name"}]
        [:input {:type :password :value (:passphrase @state) :placeholder "Passphrase"}]
        [:input {:type :button :value "Create new"}]          
        [:input {:type :button :value "Open existing"}]]
      [:div.row [:span "Properties..."]]]))          
  
(defn question-adder [state]
  (fn []
    [:div.container
      [:div.row [:span.badge.badge-info "Add a question"]]
      [:div.row
        [:input {:type :text :value (:current-question @state) :placeholder "Question"}]
        [:select]
        [:label
          [:input {:type :checkbox :value (:current-required @state)}]
          "Require an answer"]
        [:label
          [:input {:type :checkbox :value (:current-allow-na @state)}]
          "Provide Not Applicable"]]]))

(defn answer-customizer [state]
  (fn []
      [:div.container]))
       

(defn home-page []
  [:div.container
    [:li [:a {:href "/"} "Home"]]
    [:h1 "Survey Editor"]
    [name-and-passphrase-opener state]
    [:ul
      [:li "Add a survey"]
      [:li "Edit a survey"
        [:ul
          [:li "auto-save draft"]]]]
    [question-adder state]
    [:ul
      [:li "Add a question"
        [:ul
          [:li "Set the type of answer to the question"]
          [:li "Set whether the question requires an answer or not"]
          [:li "Provide Not Applicable as an answer"]]]]
    [:ul
      [:li "View types of answers"
        [:ul
          [:li "Yes / No"]
          [:li "Strongly disagree .. Strongly agree (5 levels)"]
          [:li "Custom: Zero or more of the given values, in any order"]
          [:li "Custom: Zero or more of the given values, in a given order or rank"]]]]])

(defn open-doc [surveyname passphrase] 
  (POST "/survey/doc" 
        {:params 
         {:surveyname surveyname :passphrase passphrase} 
         :handler 
         #(js/alert %)}))
;; -------------------------
;; Initialize app

(defn mount-components []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-components))
