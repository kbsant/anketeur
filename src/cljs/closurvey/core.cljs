(ns closurvey.core
  (:require
    [clojure.string :as string]
    [closurvey.client.event :as event] 
    [reagent.core :as r]
    [ajax.core :refer [GET POST]]))

(def yes-no-option
  {:option-text "Yes / No"
   :index 0
   :predefined true
   :template :radio
   :params {:values ["Yes" "No"]}})

(def agree-disagree-5-levels-option
  {:option-text "Disagree ... Agree (5 levels)"
   :index 1
   :predefined true
   :template :radio
   :params {:values ["Strongly disagree" "Disagree" "Neither agree nor disagree"
                    "Agree" "Strongly agree"]}})

(def test-multi-option
  {:option-text "Test multi"
   :index 3
   :template :checkbox
   :predefined true
   :params {:values ["Eggs" "Poultry" "Fish" "Shellfish" "Crustaceans"
                     "Meat" "Dairy" "Others"]}})

(def text-area-option
  {:option-text "Free text"
   :index 2
   :predefined true
   :template :text-area})

(def empty-question
  {:current-question-text ""
   :current-answer-type (:option-text yes-no-option)
   :current-required false
   :current-allow-na false})

(def new-question
  {:question-text ""
   :answer-type (:option-text yes-no-option)
   :required false
   :allow-na false})

(def empty-custom-answer
  {:custom-answer-name ""
   :custom-answer-items []})

(defn map-with-key
  "Transform a sequence ({:k k1 ...}, {:k k2 ...}...)
  into a map {k1 {:k k1 ...} k2 {:k k2, ...}}"
  [key submaps]
  (zipmap (map key submaps) submaps))

(defonce state
  (r/atom
    (merge
      empty-question
      {:surveyname ""
       :question-list []
       :question-index
        {:new-question (assoc new-question :index :new-question)}
       :answer-types
        (map-with-key
          :option-text
          [yes-no-option agree-disagree-5-levels-option test-multi-option text-area-option])})))

(defn question-list-view
  "question-index holds the indexed data, while the order is determined by question-list.
  To obtain a list view of the questions, de-reference the index using the question list." 
  [state-info]
  (map (:question-index state-info) (:question-list state-info)))

(defn next-question-id [question-index]
  (->> question-index
       keys
       (filter number?)
       (concat [0])
       (apply max)
       inc))

(defn add-question
  "Add a question to both the question index and list. 
  To update the state, use this function with swap! ." 
  [state-info question]
  (let [question-id (next-question-id (:question-index state-info))
        question-info (assoc question :index question-id)]
    (-> state-info
      (assoc-in [:question-index question-id] question-info)
      (update :question-list conj question-id))))

(defn open-or-edit-selector [state]
  [:div.container
    [:div.row [:span.font-weight-bold "Create or edit a survey"]]
    [:div.row
      [:input.mr-1
        {:type :text
         :value (:surveyname @state)
         :placeholder "Survey name"
         :on-change (event/assoc-with-js-value state :surveyname)}]
      [:input.mr-1
        {:type :button
         :value "Create new"}]
      [:input.mr-1
        {:type :button
         :value "Open existing"}]]
    [:div.row
      [:span "Properties..."]]])

(defn build-current-question
  [{:keys [current-question-text current-answer-type current-required current-allow-na]}]
  (when-not (string/blank? current-question-text)
    {:question-text current-question-text
     :answer-type current-answer-type
     :allow-na current-allow-na
     :required current-required}))

(defn render-select-options
  "Given a map of answer types, render a list of options
  by taking the values sorted by index."
  [answer-types]
  (->> answer-types
       vals
       (sort (comp < :index))
       (map-indexed
          (fn [i {:keys [option-text]}]
            ^{:key i}
            [:option option-text]))))


(defn question-adder [state]
  [:div.container
    [:div.row  [:span.font-weight-bold "Add/edit a question"]]
    [:div.row
      [:input.mr-1
        {:type :text
         :value (:current-question-text @state)
         :placeholder "Question"
         :on-change (event/assoc-with-js-value state :current-question-text)}]
      [:select.mr-1
        {:value (:current-answer-type @state)
         :on-change (event/assoc-with-js-value state :current-answer-type)}
        (render-select-options (:answer-types @state))]
      [:label.mr-1
        [:input.mr-1
          {:type :checkbox
           :checked (:current-required @state)
           :value (:current-required @state)
           :on-click (event/update-with-js-value state :current-required event/js-not)}]
        "Require an answer"]
      [:label.mr-1
        [:input.mr-1
          {:type :checkbox
           :checked (:current-allow-na @state)
           :value (:current-allow-na @state)
           :on-click (event/update-with-js-value state :current-allow-na event/js-not)}]
        "Provide Not Applicable"]
      [:input {:type :button
               :value "Add"
               :on-click #(when-let [q (build-current-question @state)]
                            (swap! state add-question q)
                            (swap! state merge empty-question))}]]])

(defn edit-question
  [state i {:keys [question-text answer-type required allow-na index] :as question}]
  ^{:key i}
  [:div.container
    [:div.row
      [:span.mr-1.font-weight-bold (str (inc i))]
      [:input.mr-1
        {:type :text
         :value question-text
         :placeholder "Question"
         :on-change (event/assoc-in-with-js-value
                      state
                      [:question-index index :question-text])}]
      [:select.mr-1
        {:value answer-type
         :on-change (event/assoc-in-with-js-value
                      state
                      [:question-index index :answer-type])}
        (render-select-options (:answer-types @state))]
      [:label.mr-1
        [:input.mr-1
          {:type :checkbox
           :checked required
           :value required
           :on-change #(swap!
                        state update-in
                        [:question-index index :required]
                        not)}]
        "Require an answer"]
      [:label.mr-1
        [:input.mr-1
          {:type :checkbox
           :checked allow-na
           :value allow-na
           :on-change #(swap!
                        state update-in
                        [:question-index index :allow-na]
                        not)}]
        "Provide Not Applicable"]]])

(defn answer-customizer [state]
  [:div.container
    [:div.row [:span.font-weight-bold "Add/edit a custom answer type"]]
    [:div.row
      [:input.mr-1 {:type :text :placeholder "Name of the answer type"}]
      [:select
        [:option "Single selection"]
        [:option "Multiple selection"]]]
    [:div.row
      [:input.mr-1 {:type :text :placeholder "Answer text"}]
      [:input {:type :button :value "Add"}]]])

(defn render-template-radio-or-checkbox [radio-or-checkbox {:keys [values]}]
  (fn [index {:keys [allow-na]}]
    (let [input-name (str index)]
      [:form.inline
        (map-indexed
          (fn [i input-value]
            ^{:key i}
            [:label.mr-1
              [:input.mr-1 {:type radio-or-checkbox :name input-name :value input-value}]
              input-value])
          values)
        (when (and allow-na (= :radio radio-or-checkbox))
          [:label.mr-1
            [:input.mr-1 {:type :radio :name (str index) :value "Not applicable"}]
            "Not applicable"])])))

(defn render-answer-text-area [_ _]
  [:form.inline
    [:textarea]])

(def answer-templates
  {:radio (partial render-template-radio-or-checkbox :radio)
   :checkbox (partial render-template-radio-or-checkbox :checkbox)
   :text-area (fn [_] render-answer-text-area)})

(defn render-answer-type [state-info answer-type]
  (let [{:keys [template params]} (get-in state-info [:answer-types answer-type])
        render-fn (get answer-templates template)]
    (when render-fn
      (render-fn params))))

(defn preview-question
  [state-info index {:keys [question-text answer-type required allow-na] :as question}]
  ^{:key index}
  [:div.row
      [:p
        [:span.mr-1.font-weight-bold (str (inc index))]
        question-text
        (when required [:span.alert.alert-info.ml-1.pl-1 "* Required"])]
      (when-let [answer-renderer (render-answer-type state-info answer-type)]
        (answer-renderer index question))])

(defn question-list [state]
  (fn []
    (let [questions (question-list-view @state)
          render-question (if (:question-edit-mode @state) 
                              (partial edit-question state)
                              (partial preview-question @state))]
      [:div.container
        [:div.row
          [:span.font-weight-bold (str "Question List (" (count questions) ")")]
          [:input.mr-1
            {:type :button
             :on-click #(swap! state assoc :question-edit-mode false) 
             :value "Preview mode"}]
          [:input.mr-1
            {:type :button
             :on-click #(swap! state assoc :question-edit-mode true) 
             :value "Edit mode"}]]
        (when-not (empty? questions)
          (doall
            (map-indexed render-question questions)))])))

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
    [question-list state]
    [:ul
      [:li "Questions"
        [:ul
          [:li "Allow move up/down"]
          [:li "Allow cut / copy / paste / delete"]]]]
    [question-adder state]
    [:ul
      [:li "Add/edit a question"
        [:ul
          [:li "Set the type of answer to the question"]
          [:li "Set whether the question requires an answer or not"]
          [:li "Provide Not Applicable as an answer"]
          [:li "Validate/sanitize free text fields, numbers, dates"]]]]
    [answer-customizer state]
    [:ul
      [:li "Add/edit types of answers"
        [:ul
          [:li "Customize single selection answers"]
          [:li "Customize multiple selection answers"]
          [:li "Customize named scale 1 (Least something*) to n (Most something*)"]
          [:li "Ranking of items -- drag and drop? move up/down?"]
          [:li "Don't edit predefined answer types"
            [:ul
              [:li "Yes / No"]
              [:li "Strongly disagree .. Strongly agree (5 levels)"]
              [:li "Free text"]]]]]]])

(defn open-doc [surveyname] 
  (POST "/survey/doc"
        {:params 
         {:surveyname surveyname} 
         :handler 
         #(js/alert %)}))
;; -------------------------
;; Initialize app

(defn mount-components []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-components))

