(ns closurvey.core
  (:require
    [clojure.string :as string]
    [closurvey.client.event :as event] 
    [reagent.core :as r]
    [ajax.core :refer [GET POST]]))

(def empty-question
  {:current-question-text ""
   :current-answer-type "yes-no"
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
  [{:keys [current-question-text current-answer-type current-required current-allow-na]}]
  (when-not (string/blank? current-question-text)
    {:question-text current-question-text
     :answer-type current-answer-type
     :allow-na current-allow-na
     :required current-required}))
  
(defn question-adder [state]
  (fn []
    [:div.container
      [:div.row  [:span.font-weight-bold "Add a question"]]
      [:div.row
        [:input {:type :text 
                 :value (:current-question-text @state) 
                 :placeholder "Question"
                 :on-change (event/assoc-with-js-value state :current-question-text)}]
        [:select {:on-change (event/assoc-with-js-value state :current-answer-type)}
          [:option {:value "yes-no"
                    :checked true}
                   "Yes / No"]
          [:option {:value "disagree-agree-5-levels"}
                   "Disagree ... Agree (5 levels)"]]

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

(def answer-types
  {"yes-no"
    (fn [index {:keys [allow-na]}]
      [:form.inline
        [:label.mr-1
          [:input.mr-1 {:type :radio :name (str index) :value "yes"}]
          "Yes"] 
        [:label.mr-1
          [:input.mr-1 {:type :radio :name (str index) :value "no"}]
          "No"] 
        (when allow-na 
           [:label.mr-1
             [:input.mr-1 {:type :radio :name (str index) :value "Not applicable"}]
             "Not applicable"])])
    "disagree-agree-5-levels"
      (fn [index {:keys [allow-na]}]
        [:form.inline
          [:label.mr-1
            [:input.mr-1 {:type :radio :name (str index) :value "strongly-disagree"}]
            "Strongly disagree"] 
          [:label.mr-1
            [:input.mr-1 {:type :radio :name (str index) :value "disagree"}]
            "Strongly disagree"] 
          [:label.mr-1
            [:input.mr-1 {:type :radio :name (str index) :value "neither-agree-nor-disagree"}]
            "Neither agree nor disagree"] 
          [:label.mr-1
            [:input.mr-1 {:type :radio :name (str index) :value "agree"}]
            "Agree"] 
          [:label.mr-1
            [:input.mr-1 {:type :radio :name (str index) :value "strongly-agree"}]
            "Strongly agree"]
          (when allow-na 
            [:label.mr-1
              [:input.mr-1 {:type :radio :name (str index) :value "Not applicable"}]
              "Not applicable"])])})
 
(defn render-question 
  [index {:keys [question-text answer-type required allow-na] :as question}]
  ^{:key index}
  [:div.row
    (list
      [:p
        [:span.mr-1.font-weight-bold (str (inc index))] 
        question-text 
        (when required [:span.alert.alert-info.ml-1.pl-1 "* Required"])]
      (when-let [answer-renderer (get answer-types answer-type)]
        (answer-renderer index question))
      (when allow-na "Allow n/a"))]) 
       
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
