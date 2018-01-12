(ns closurvey.core
  (:require
    [clojure.string :as string]
    [closurvey.client.event :as event] 
    [reagent.core :as r]
    [ajax.core :refer [GET POST]]))

(def empty-question
  {:current-question ""
   :current-required false
   :current-allow-na false})

(defonce state 
  (r/atom
    (merge 
      empty-question
      {:surveyname ""
       :questions []}))) 

(defn open-or-edit-selector [state]
  (fn []
    [:div.container
      [:div.row [:span.font-weight-bold "Create or edit a survey"]]
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

(defn build-current-question 
  [{:keys [current-question current-answer-type current-required current-allow-na]}]
  (when-not (string/blank? current-question)
    {:question current-question
     :answer-type current-answer-type
     :allow-na current-allow-na
     :required current-required}))
  
(defn question-adder [state]
  (fn []
    [:div.container
      [:div.row  [:span.font-weight-bold "Add a question"]]
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
                 :value "Add"
                 :on-click #(when-let [q (build-current-question @state)]
                              (swap! state update :questions conj q)
                              (swap! state merge empty-question))}]]]))

(defn answer-customizer [state]
  (fn []
      [:div.container]))

(defn render-question [index {:keys [question required allow-na]}]
  ^{:key index}
  [:div.row
    [:p question (when required [:span.alert.alert-info.ml-1.pl-1 "* Required"])]])
       
(defn question-list [state]
  (fn []
    (let [questions (:questions @state)]
      (when-not (empty? questions)
        [:div.container 
          (map-indexed render-question questions)]))))

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
      [:li (str "Question list (" (count (:questions @state)) ")")
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
