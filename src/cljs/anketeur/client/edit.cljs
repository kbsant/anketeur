(ns anketeur.client.edit
  (:require
    [clojure.string :as string]
    [reagent.core :as r]
    [anketeur.model :as model]
    [anketeur.form :as form]
    [anketeur.client.ui :as ui]
    [anketeur.client.ajax :as appajax]
    [ajax.core :refer [GET POST]]))

(defonce docstate
  (atom {}))

(defonce state
  (r/atom
    (merge
      model/empty-survey-info)))

(defn doc-from-state [state-info]
  (dissoc state-info :client-state))

(defn doc-to-state [{:keys [client-state] :as state-info} doc]
  (assoc doc :client-state client-state))

(defn vswap
  "Swap items in a vector if indexes are in bounds."
  [v i1 i2]
  (if (and (< -1 i1 i2) (< i2 (count v)))
    (let [e1 (v i1), e2 (v i2)]
      (assoc v i1 e2 i2 e1))
    v))

(defn save-undo-point [event-type state-info]
  (let [doc (doc-from-state state-info)
        prev (peek (get-in state-info [:client-state :undo]))]
    (cond-> state-info
        (or (not= :text event-type) (not= :text (:event-type prev)))
        (update-in [:client-state :undo] conj (assoc doc :event-type event-type))
        true
        (assoc-in [:client-state :redo] nil))))

(defn undoable
  "Create an undoable function. The first parameter is the event type.
  The following parameters are the function, followed by arguments.
  The function is applied in the same way as swap! :
  The value of the atom is passed as the first argument, followed by
  the specified arguments."
  ([event-type f x] (undoable event-type #(f % x)))
  ([event-type f x y] (undoable event-type #(f % x y)))
  ([event-type f x y & args] (undoable event-type #(apply f % x y args)))
  ([event-type f]
   (comp f (partial save-undo-point event-type))))

(defn apply-undo [state-info]
  (let [doc (doc-from-state state-info)
        prev (peek (get-in state-info [:client-state :undo]))]
    (if-not prev
      ;; nothing in undo stack. can't undo.
      state-info
      (-> state-info
          (update-in [:client-state :redo] conj [prev doc])
          (update-in [:client-state :undo] pop)
          (doc-to-state prev)))))

(defn apply-redo [state-info]
  (let [doc (doc-from-state state-info)
        [prev next] (peek (get-in state-info [:client-state :redo]))]
    (if-not (= doc prev)
      ;; doc has changed. can't redo.
      state-info
      (-> state-info
          (update-in [:client-state :undo] conj doc)
          (update-in [:client-state :redo] pop)
          (doc-to-state next)))))

(defn clipboard-to-trash [state-info]
  (let [clipboard (get-in state-info [:clipboard :question-list])]
    (if (empty? clipboard)
      state-info
      (-> state-info
          (assoc-in [:clipboard :question-list] [])
          (update-in [:trash :question-list] into clipboard)))))

(defn cut-question [{:keys [edit-index] :as state-info}]
  (-> state-info
      (update :question-list #(->> % (remove #{edit-index}) (into [])))
      clipboard-to-trash
      (assoc-in [:clipboard :question-list] [edit-index])))

(defn copy-question [{:keys [edit-index] :as state-info}]
  (let [question (get-in state-info [:question-map edit-index])
        [question-id updated-info] (model/clone-question state-info question)]
    (assoc-in updated-info [:clipboard :question-list] [question-id])))

(defn paste-question [ord {:keys [edit-index clipboard] :as state-info}]
  (let [cut-data (:question-list clipboard)]
    (-> state-info
        (assoc-in [:clipboard :question-list] [])
        (update :question-list (partial model/insert-vec cut-data ord)))))

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
       :handler #(do (swap! state assoc-in [:client-state :save-status] %)
                     (fade-save-status state)
                     (when handler-fn (handler-fn)))
       :error-handler
         #(swap! state assoc-in [:client-state :error-status]
                 "The session has expired. Please reload the page.")}))))

(defn save-and-export! [uri]
  (save-doc! #(.open js/window uri)))

(defn save-if-changed! []
  (let [doc (doc-from-state @state)]
    (when (not= doc @docstate)
      (save-doc!))))

(defn save-button-status [state]
  (let [{:keys [save-status error-status]} (:client-state @state)]
     [:form.inline
      [:input.mr-1
        {:type :button
         :value "Save"
         :on-click #(save-doc!)}]
      [:span
        {:style (ui/fade-opacity save-status)}
        save-status]
      (when-not (string/blank? error-status) [:span.alert.alert-danger error-status])]))


(defn save-control-group [state]
  (let [{:keys [client-state surveyname surveyno description]} @state
        {:keys [export-link-base response-link-base save-status error-status]} client-state
        export-uri (str export-link-base "EDN/id/" surveyno)
        response-uri (str response-link-base surveyno)]
    [:div.container
      [:div.row [:span.font-weight-bold "Edit a survey"]]
      [:div.row
        [:input.mr-1
          {:type :text
           :value surveyname
           :placeholder "Survey name"
           :on-change #(swap! state (undoable :text (ui/js-assoc-fn :surveyname %)))}]
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
      (when-not (string/blank? error-status)
        [:div.row
          [:span.alert.alert-danger error-status]])
      [:div.row
        [:span "Properties..."]]
      [:div.row
       [:div.col-8
        [:textarea.w-100
          {:placeholder "Description"
           :value description
           :on-change #(swap! state (undoable :text (ui/js-assoc-fn :description %)))}]]
       [:div.col]]]))

(defn render-select-options
  "Given a map of answer types, render a list of options
  by taking the values sorted by index."
  [answer-types]
  (->> answer-types
       vals
       (sort-by :option-text)
       (map-indexed
          (fn [i {:keys [custom-index option-text]}]
            ^{:key i}
            [:option (when custom-index {:value custom-index}) option-text]))))

(defn answer-text-input-fn [state custom-index {:keys [values text] :as params}]
  (let [edit-text (or text (string/join "\n" values) "")
        rows (-> (count values) (or 1) inc)]
    [:textarea.mr-1.w-75
      {:style {:width "75%"}
       :placeholder "(Options)"
       :rows rows
       :value edit-text
       :on-change
        #(swap! state
          (undoable :text
            (ui/js-update-in-fn
              [:answer-types custom-index :params] model/update-text-answer-params %)))}]))

(defn update-num-answer-params-js [params value]
  (let [max (when value (js/parseInt value))]
    (model/update-num-answer-params params max)))

(defn answer-num-input-fn [state custom-index params]
  (let [default-value 5
        value (get-in params [:range 0] default-value)]
    [:input.mr-1
      {:type :number
       :min 2
       :max 20
       :placeholder "Max rating"
       :value value
       :on-change
          #(swap! state
            (undoable :text
              (ui/js-update-in-fn
                [:answer-types custom-index :params] update-num-answer-params-js %)))}]))

(def answer-param-input-renderers
  {:text answer-text-input-fn
   :rating answer-num-input-fn})

(defn answer-param-customizer [state current-answer]
  (let [{:keys [custom-index custom-template param-type params]} current-answer
        render-fn (get answer-param-input-renderers param-type)]
    (when render-fn
      (render-fn state custom-index params))))

(defn answer-customizer [state]
  (let [question-index (:edit-index @state)
        answer-index (get-in @state [:question-map question-index :answer-type])
        current-answer (get-in @state [:answer-types answer-index])
        custom-template (:custom-template current-answer)
        predefined? (true? (:predefined current-answer))]
    [:div.container
      [:div.row [:span.font-weight-bold "Custom answer type"]]
      [:div.row
        [:input.mr-1
          {:type :text
           :disabled predefined?
           :placeholder "Name of the answer type"
           :value (:option-text current-answer)
           :on-change
             #(swap! state
                (undoable :text
                  (ui/js-assoc-in-fn [:answer-types answer-index :option-text] %)))}]
        [:select
          (merge
            {:disabled predefined?
             :on-change
               #(swap! state
                  (undoable :select
                    (ui/js-update-in-fn
                      [:answer-types answer-index] model/merge-from-template %)))}
            (when custom-template {:value custom-template}))
          (render-select-options model/answer-template-options)]]
      [:div.row
        (answer-param-customizer state current-answer)]]))

(defn add-or-select-answer-type [question-index ev state-info]
  (let [answer-index (ui/target-value ev)]
     (if (= "custom-answer-type" answer-index)
       (model/add-answer-type state-info)
       (model/select-answer-type question-index answer-index state-info))))

(defn edit-question
  [state ord question]
  (let [merged (merge model/blank-question question)
        {:keys [pos question-text answer-type required allow-na skip index]} merged
        move-up-fn #(update % :question-list vswap (dec ord) ord)
        move-down-fn #(update % :question-list vswap ord (inc ord))
        paste-fn (partial paste-question ord)
        to-trash-fn (partial model/move-question-to-trash index)
        clipboard-empty?  (empty? (get-in @state [:clipboard :question-list]))]
   ^{:key index}
    [:div.container
      [:div.row
        [:input.mr-1.mb-1
         {:type :button
          :value "↑"
          :on-click #(swap! state (undoable :move move-up-fn))}]
        [:input.mr-1.mb-1
         {:type :button
          :value "↓"
          :on-click #(swap! state (undoable :move move-down-fn))}]
        [:input.mr-1.mb-1
         {:type :button
          :value "Cut"
          :on-click #(swap! state (undoable :cut cut-question))}]
        [:input.mr-1.mb-1
         {:type :button
          :value "Copy"
          :on-click #(swap! state (undoable :cut copy-question))}]
        [:input.mr-1.mb-1
         {:type :button
          :value "Paste"
          :disabled clipboard-empty?
          :on-click #(swap! state (undoable :cut paste-fn))}]
        [:input.mr-1.mb-1
         {:type :button
          :value "×"
          :on-click #(swap! state (undoable :trash to-trash-fn))}]]
      [:div.row
        [:span.mr-1.font-weight-bold (str pos)]
        [:input.mr-1.w-60
          {:style {:width "70%"}
           :type :text
           :value question-text
           :placeholder "Question"
           :on-change #(swap! state
                         (undoable :text
                          (ui/js-assoc-in-fn [:question-map index :question-text] %)))}]]
      [:div.row
        [:label.mr-1
          [:input.mr-1
            {:type :checkbox
             :checked required
             :value required
             :on-change
               #(swap! state
                  (undoable :check update-in [:question-map index :required] not))}]
          "Require an answer"]
        [:label.mr-1
          [:input.mr-1
            {:type :checkbox
             :checked allow-na
             :value allow-na
             :on-change
               #(swap! state
                  (undoable :check update-in [:question-map index :allow-na] not))}]
          "Provide Not Applicable"]
        [:label.mr-1
          [:input.mr-1
            {:type :checkbox
             :checked skip
             :value skip
             :on-change
               #(swap! state (undoable :check update-in [:question-map index :skip] not))}]
          "Skip numbering (child item)"]
        [:select.mr-1
          {:value answer-type
           :on-change
             #(swap! state (undoable :select (partial add-or-select-answer-type index %)))}
          (render-select-options (:answer-types @state))]]
      [:div.row
        [answer-customizer state]]]))

(defn question-adder [state]
  (let [new-question (model/get-new-question @state)]
    [:div.container
      [:br]
      [:input {:type :button
               :value "Add a new question"
               :on-click #(swap! state
                            (undoable :select model/add-question model/new-question))}]]))

(defn update-edit-index [index state-info]
  (update state-info :edit-index #(if (= % index) -1 index)))

(defn toggle-edit-question [state ord {:keys [index] :as question}]
  (let [active (= index (:edit-index @state))
        toggle-fn (partial update-edit-index index)]
    ^{:key index}
    [:div.row
      [:div.col-xs-1
        [:input
          {:type :button
           :value (if active "»" " ")
           :on-click #(swap! state (undoable :ui toggle-fn))}]]
      [:div.col-xs-11
        (if active
          (edit-question state ord question)
          (form/preview-question @state question))]]))

(defn question-list [state]
  (fn []
    (let [questions (model/question-list-view @state)
          total (count questions)
          numbered (count (remove :skip questions))
          render-question (partial toggle-edit-question state)]
      [:div.container
        [:div.row
          [:span.font-weight-bold
            (when (not= numbered total)
              (str "Numbered (" numbered ") / "))
            (str "Total (" total ")")]]
        (when-not (empty? questions)
          (doall
            (map-indexed render-question questions)))
        [question-adder state]])))

(defn trash-list [state]
  (fn []
    (let [state-info @state
          {:keys [trash question-map]} state-info
          {:keys [question-list]} trash
          questions (map #(get question-map %) question-list)
          unused-answers (model/unused-answer-types state-info)]
      [:div.container.trash-area
        [:p
          (if (empty? question-list)
            "No deleted questions."
            [:span.font-weight-bold "Deleted Questions"])]
        (map-indexed
          (fn [i {:keys [index] :as question}]
            ^{:key i}
            [:div.row
              [:div.col-xs-1
                [:input
                  {:type :button
                   :value "←"
                   :on-click
                    #(swap! state
                       (undoable :trash (partial model/move-question-from-trash index)))}]]
              [:div.col-xs-1
                [:input
                  {:type :button
                   :value "x"
                   :on-click
                    #(swap! state
                      (undoable :trash (partial model/purge-question-from-trash index)))}]]
              [:div.col-xs-10 (form/preview-question state-info question)]])
          questions)
        (when (seq unused-answers)
          [:p.font-weight-bold "Unused Answer Types"])
        (map-indexed
          (fn [i {:keys [custom-index option-text] :as answer-type-info}]
            ^{:key i}
            [:div.row
              [:div.col-xs-1]
              [:div.col-xs-1
                [:input
                  {:type :button
                   :value "x"
                   :on-click
                    #(swap! state
                      (undoable :trash (partial model/purge-answer-type custom-index)))}]]
              [:div.col-xs-10
                [:p option-text]
                (when-let [render-fn (form/render-answer-type answer-type-info)]
                  (render-fn i nil nil))]])
          unused-answers)])))

(defn toggle-trash-button [state]
  (let [trash-visible? (= :trash (get-in @state [:client-state :view]))
        next-state (if trash-visible? :questions :trash)
        toggle-fn #(assoc-in % [:client-state :view] next-state)]
    [:input
     {:type :button
      :value (if trash-visible? "Hide Trash" "View Trash")
      :on-click #(swap! state toggle-fn)}]))

(defn home-page []
  [:div.container
    [:h1 "Survey Editor"]
    [save-control-group state]
    [:input
     {:type :button
      :value "Undo"
      :on-click #(swap! state apply-undo)}]
    [:input
     {:type :button
      :value "Redo"
      :on-click #(swap! state apply-redo)}]
    [toggle-trash-button state]
    (when (= :trash (get-in @state [:client-state :view]))
      [trash-list state])
    [question-list state]
    [:br]
    [save-button-status state]
    [:br]])

; -------------------------
;; Initialize app

(defn mount-components []
  (r/render [home-page] (.getElementById js/document "app")))

(defn load-transit! []
  (let [init-state (ui/read-json js/transitState)
        default-anwer-types (:answer-types model/empty-survey-info)
        init-survey-info (:survey-info init-state)
        survey-info (update init-survey-info :answer-types merge default-anwer-types)
        init-client-state (select-keys init-state [:response-link-base :export-link-base :flash-errors])]
    (swap! state merge survey-info)
    (swap! state update :client-state merge init-client-state)
    (reset! docstate (doc-from-state @state))))

(defn ^:export init []
  (appajax/load-interceptors! js/context js/csrfToken)
  (mount-components)
  (load-transit!)
  (ui/repeat-timer save-if-changed! 60000))

