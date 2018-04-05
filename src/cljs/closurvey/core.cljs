(ns closurvey.core
  (:require
    [clojure.string :as string]
    [closurvey.client.event :as event] 
    [reagent.core :as r]
    [closurvey.model :as model]
    [closurvey.client.surveyform :as form]
    [closurvey.client.ui :as ui]
    [closurvey.ajax :as appajax]
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

(def rating-5-levels-option
  {:option-text "Rating (5 levels)"
   :index 2
   :predefined true
   :template :radio
   :params {:values ["1" "2" "3" "4" "5"]}})

(def text-area-option
  {:option-text "Free text"
   :index 3
   :predefined true
   :template :text-area})

(def empty-question
  {:current-question-text ""
   :current-answer-type (:option-text yes-no-option)
   :current-skip false
   :current-required false
   :current-allow-na false})

(def new-question
  {:question-text ""
   :answer-type (:option-text yes-no-option)
   :skip false
   :required false
   :allow-na false})

(defn map-with-key
  "Transform a sequence ({:k k1 ...}, {:k k2 ...}...)
  into a map {k1 {:k k1 ...} k2 {:k k2, ...}}"
  [key submaps]
  (zipmap (map key submaps) submaps))

(def answer-template-options
  (map-with-key
    :option-text
    [{:index 0, :option-text "Single selection", :template :radio, :param-type :text}
     {:index 1, :option-text "Multiple selection", :template :checkbox, :param-type :text}
     {:index 2, :option-text "Rating", :template :radio, :param-type :rating}]))

(def empty-custom-answer
  {:custom-answer-name ""
   :custom-answer-num-input nil
   :custom-answer-text-input [""]
   :custom-answer-type (first (keys answer-template-options))
   :custom-answer-items []})

(defonce docstate
  (atom {}))

(defonce state
  (r/atom
    (merge
      empty-question
      empty-custom-answer
      {:surveyname ""
       :client-state {}
       :question-list []
       :question-map
        {:new-question (assoc new-question :index :new-question)}
       :answer-types
        (map-with-key
          :option-text
          [yes-no-option agree-disagree-5-levels-option rating-5-levels-option text-area-option])})))

(defn doc-from-state [state-info]
  (dissoc state-info :client-state))

(defn vswap
  "Swap items in a vector if indexes are in bounds."
  [v i1 i2]
  (if (and (< -1 i1 i2) (< i2 (count v)))
    (let [e1 (v i1), e2 (v i2)]
      (assoc v i1 e2 i2 e1))
    v))

(defn fade-save-status [state]
   (ui/single-timer
     #(swap! state assoc-in [:client-state :save-status] "")
     3000))

;; TODO queuing and auto-save and status display and async stuff
(defn save-doc!
 ([]
  (save-doc! nil))
 ([handler-fn]
  (let [doc (doc-from-state @state)]
    (reset! docstate doc)
    (POST
      "/save"
      {:params {:survey-info doc}
       ;; TODO fade out after saving
       :handler #(do (swap! state assoc-in [:client-state :save-status] %)
                     (fade-save-status state)
                     (when handler-fn (handler-fn)))
       :error-handler #(swap! state assoc-in [:client-state :save-status] (str %))}))))

(defn save-and-export! [uri]
  (save-doc! #(.open js/window uri)))

(defn save-if-changed! []
  (let [doc (doc-from-state @state)]
    (when (not= doc @docstate)
      (save-doc!))))

(defn save-control-group [state]
  (let [{:keys [client-state surveyname surveyno description]} @state
        {:keys [export-link-base response-link-base save-status]} client-state
        export-uri (str export-link-base "EDN/id/" surveyno)
        response-uri (str response-link-base surveyno)]
    [:div.container
      [:div.row [:span.font-weight-bold "Edit a survey"]]
      [:div.row
        [:input.mr-1
          {:type :text
           :value surveyname
           :placeholder "Survey name"
           :on-change (event/assoc-with-js-value state :surveyname)}]
        [:input.mr-1
          {:type :button
           :value "Save"
           :on-click #(save-doc!)}]
        [:input.mr-1
          {:type :button
           :value "Save and export"
           :on-click #(save-and-export! export-uri)}]
        [:input.mr-1
          {:type :button
           :value "Link to respond to this survey"
           :on-click #(.open js/window response-uri)}]
        [:span
           {:style (ui/fade-opacity save-status)}
           save-status]]
      [:div.row
        [:span "Properties..."]]
      [:div.row
       [:div.col-xs-8
        [:textarea.mr-1w-75
          {:style {:width "75%"}
           :placeholder "Description"
           :value description
           :on-change (event/assoc-with-js-value state :description)}]]]]))

(defn build-current-question
  [{:keys [current-question-text current-answer-type current-required current-allow-na current-skip]}]
  (when-not (string/blank? current-question-text)
    {:question-text current-question-text
     :answer-type current-answer-type
     :allow-na current-allow-na
     :skip current-skip
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
           :on-change #(swap! state update :current-required not)}]
        "Require an answer"]
      [:label.mr-1
        [:input.mr-1
          {:type :checkbox
           :checked (:current-allow-na @state)
           :value (:current-allow-na @state)
           :on-change #(swap! state update :current-allow-na not)}]
        "Provide Not Applicable"]
      [:label.mr-1
        [:input.mr-1
          {:type :checkbox
           :checked (:current-skip @state)
           :value (:current-skip @state)
           :on-change #(swap! state update :current-skip not)}]
        "Skip numbering (child item)"]
      [:input {:type :button
               :value "Add"
               :on-click #(when-let [q (build-current-question @state)]
                            (swap! state model/add-question q)
                            (swap! state merge empty-question))}]]])

(defn edit-question
  [state ord {:keys [pos question-text answer-type required allow-na skip index] :as question}]
  ^{:key index}
  [:div.container
    [:div.row
      [:input.mr-1
       {:type :button
        :value "↑"
        :on-click #(swap! state update :question-list vswap (dec ord) ord)}]
      [:input.mr-1
       {:type :button
        :value "↓"
        :on-click #(swap! state update :question-list vswap ord (inc ord))}]
      [:span.mr-1.font-weight-bold (str pos)]
      [:input.mr-1
        {:type :text
         :value question-text
         :placeholder "Question"
         :on-change (event/assoc-in-with-js-value
                      state
                      [:question-map index :question-text])}]
      [:select.mr-1
        {:value answer-type
         :on-change (event/assoc-in-with-js-value
                      state
                      [:question-map index :answer-type])}
        (render-select-options (:answer-types @state))]
      [:label.mr-1
        [:input.mr-1
          {:type :checkbox
           :checked required
           :value (true? required)
           :on-change #(swap!
                        state update-in
                        [:question-map index :required]
                        not)}]
        "Require an answer"]
      [:label.mr-1
        [:input.mr-1
          {:type :checkbox
           :checked allow-na
           :value (true? allow-na)
           :on-change #(swap!
                        state update-in
                        [:question-map index :allow-na]
                        not)}]
        "Provide Not Applicable"]
      [:label.mr-1
        [:input.mr-1
          {:type :checkbox
           :checked skip
           :value (true? skip)
           :on-change #(swap!
                        state update-in
                        [:question-map index :skip]
                        not)}]
        "Skip numbering (child item)"]]])

(defn answer-text-input-fn [state]
  (let [custom-answer-text-input (concat (:custom-answer-text-input @state) [""])]
   [:form
    (map-indexed
      (fn [i v]
        ^{:key i}
        [:input.mr-1
          {:type :text
           :placeholder "Answer text"
           :value v
           :on-change
            (event/assoc-in-with-js-value
              state
              [:custom-answer-text-input i])}])
      custom-answer-text-input)]))

(defn answer-text-value-fn [state-info]
  (let [value (:custom-answer-text-input state-info)]
    (when (and
            (seq value)
            (not (string/blank? (first value))))
      value)))

(defn answer-num-input-fn [state]
  [:input.mr-1 {:type :number
                :min 2
                :max 20
                :placeholder "Max rating"
                :value (:custom-answer-num-input @state)
                :on-change
                  (event/assoc-with-js-value
                    state
                    :custom-answer-num-input)}])

(defn answer-num-value-fn [state-info]
  (let [value (:custom-answer-num-input state-info)
        max (and value (js/parseInt value))]
    (when max
      (into [] (map str (range 1 (inc max)))))))

(def custom-answer-input-fn
  {:text
    {:render-fn answer-text-input-fn
     :value-fn answer-text-value-fn}
   :rating
    {:render-fn answer-num-input-fn
     :value-fn answer-num-value-fn}})

(defn build-custom-answer
  [{:keys [custom-answer-type custom-answer-name custom-answer-params answer-types]
    :as state-info}]
  (let [template (get-in answer-template-options [custom-answer-type :template])
        param-type (get-in answer-template-options [custom-answer-type :param-type])
        value-fn (get-in custom-answer-input-fn [param-type :value-fn])
        custom-answer-params (value-fn state-info)
        index (->> answer-types vals (map :index) (concat [0]) last)]
    (when (and
            (not (string/blank? custom-answer-name))
            (seq custom-answer-params)
            template index)
      {:option-text custom-answer-name
       :index index
       :template template
       :params {:values custom-answer-params}})))

(defn render-custom-answer-input [state]
  (let [custom-answer-type (:custom-answer-type @state)
        param-type (get-in answer-template-options [custom-answer-type :param-type])
        render-fn (get-in custom-answer-input-fn [param-type :render-fn])]
    (when render-fn
      (render-fn state))))

(defn answer-customizer [state]
  [:div.container
    [:div.row [:span.font-weight-bold "Add/edit a custom answer type"]]
    [:div.row
      [:input.mr-1
       {:type :text
        :placeholder "Name of the answer type"
        :value (:custom-answer-name @state)
        :on-change (event/assoc-with-js-value state :custom-answer-name)}]
      [:select
        {:value (:custom-answer-type @state)
         :on-change (event/assoc-with-js-value state :custom-answer-type)}
        (render-select-options answer-template-options)]]
    [:div.row
      [render-custom-answer-input state]
      [:input {:type :button
               :value "Add"
               :on-click #(when-let [new-answer (build-custom-answer @state)]
                            (swap! state
                                   assoc-in
                                   [:answer-types (:option-text new-answer)]
                                   new-answer)
                            (swap! state merge empty-custom-answer))}]]])

(defn question-list [state]
  (fn []
    (let [questions (model/question-list-view @state)
          render-question (if (:question-edit-mode @state) 
                              (partial edit-question state)
                              #(form/preview-question @state %2))]
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
    [save-control-group state]
    [:ul
      [:li "Edit a survey"
        [:ul
          [:li "auto-save draft"]
          [:li "import/export EDN"]
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
          [:li "Option whether or not to display the item number"]
          [:li "Allow non-question item types like section names and comments."]
          [:li "Indent an item and optionally show bullet points instead of item numbers"]
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
          [:li "Add drop-down to select an answer type to edit"]
          [:li "Don't edit predefined answer types"
            [:ul
              [:li "Yes / No"]
              [:li "Strongly disagree .. Strongly agree (5 levels)"]
              [:li "Free text"]]]]]]])

; -------------------------
;; Initialize app

(defn mount-components []
  (r/render [home-page] (.getElementById js/document "app")))

(defn load-transit! []
  (let [init-state (ui/read-transit-state js/transitState)
        survey-info (:survey-info init-state)
        init-client-state (select-keys init-state [:response-link-base :export-link-base :flash-errors])]
    (swap! state merge survey-info)
    (swap! state update :client-state merge init-client-state)
    (reset! docstate (doc-from-state @state))))

(defn ^:export init []
  (appajax/load-interceptors! js/context js/csrfToken)
  (mount-components)
  (load-transit!)
  (ui/repeat-timer save-if-changed! 60000))

