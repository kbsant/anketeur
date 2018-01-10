(ns closurvey.core
  (:require
    [closurvey.client.event :as event] 
    [reagent.core :as r]
    [ajax.core :refer [GET POST]]))

(defonce state 
  (r/atom
    {:surveyname ""
     :current-question ""
     :current-required false
     :current-allow-na false
     :questions []})) 

(defn open-or-edit-selector [state]
  (fn []
    [:div.container
      [:div.row [:span.badge.badge-info "Create or edit a survey"]]
      [:div.row
        [:input {:type :text 
                 :value (:surveyname @state) 
                 :placeholder "Survey name"
                 :on-change (event/assoc-with-js-value state :surveyname)}]
        [:input {:type :button 
                 :value "Create new"}]          
        [:input {:type :button 
                 :value "Open existing"}]]
      [:div.row 
        [:span "Properties..."]]]))          
  
(defn question-adder [state]
  (fn []
    [:div.container
      [:div.row [:span.badge.badge-info "Add a question"]]
      [:div.row
        [:input {:type :text 
                 :value (:current-question @state) 
                 :placeholder "Question"
                 :on-change (event/assoc-with-js-value state :current-question)}]
        [:select]
        [:label
          [:input {:type :checkbox 
                   :checked (:current-required @state)
                   :value (:current-required @state)
                   :on-click (event/update-with-js-value state :current-required not)}]
          "Require an answer"]
        [:label
          [:input {:type :checkbox 
                   :checked (:current-allow-na @state)
                   :value (:current-allow-na @state)
                   :on-click (event/update-with-js-value state :current-allow-na not)}]
          "Provide Not Applicable"]
        [:input {:type :button
                 :value "Add"}]]]))

(defn answer-customizer [state]
  (fn []
      [:div.container]))
       
(defn question-list [state]
  (fn []
    (let [questions (:questions @state)]
      (when-not (empty? questions)
        (->> questions
             (map-indexed
                (fn [i q]
                  ^{:key i}
                  [:div.row
                    [:p (:question q)]])))))))


(defn home-page []
  [:div.container
    [:li [:a {:href "/"} "Home"]]
    [:h1 "Survey Editor"]
    [open-or-edit-selector state]
    [:ul
      [:li "Add a survey"]
      [:li "Edit a survey"
        [:ul
          [:li "auto-save draft"]
          [:li "publish questionnaire"]]]]
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
          [:li "Custom: Zero or more of the given values, in a given order or rank"]]]]
    [question-list state]
    [:ul
      [:li "Question list"
        [:ul
          [:li "Allow move up/down"]
          [:li "Allow cut / copy / paste / delete"]]]]])
          


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
