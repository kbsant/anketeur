(ns anketeur.model)

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

(def static-option
  {:option-text "Static text/comment"
   :index 4
   :predefined true
   :template :static})

(def add-custom-option
  {:option-text "(Add custom answer)"
   :custom-index "custom-answer-type"
   :index 99
   :predefined true
   :template :static})

(def blank-option
  {:option-text "(New answer type)"
   :custom-template "Single selection"
   :template :static})

(def blank-question
  {:question-text ""
   :answer-type (:option-text yes-no-option)
   :skip false
   :required false
   :allow-na false})

(def new-question
  (assoc blank-question :index :new-question))

(defn map-with-key
  "Transform a sequence ({:k k1 ...}, {:k k2 ...}...)
  into a map {k1 {:k k1 ...} k2 {:k k2, ...}}"
  [key submaps]
  (zipmap (map key submaps) submaps))

(def answer-template-options
  (map-with-key
    :option-text
    [{:index 0
      :option-text "Single selection"
      :template :radio
      :param-type :text
      :params {:values []}}
     {:index 1
      :option-text "Multiple selection"
      :template :checkbox
      :param-type :text
      :params {:values []}}
     {:index 2
      :option-text "Rating"
      :template :radio
      :param-type :rating
      :params {:range [5]}}]))

(defn merge-from-template
  "Set the template of the target. Preserve the params if already set."
  [target custom-template]
  (let [base (select-keys
               (get answer-template-options custom-template)
               [:template :param-type :params])
        save-params (or (:params target) (:params base))]
    (-> target
        (merge base {:custom-template custom-template})
        (assoc :params save-params))))

(def empty-survey-info
      {:surveyname ""
       :client-state {}
       :question-list []
       :question-map
        {:new-question new-question}
       :answer-types
        (map-with-key
          :option-text
          [yes-no-option agree-disagree-5-levels-option rating-5-levels-option text-area-option static-option add-custom-option])})

(defn outline-pos
  "Assign a numerical position to each question in a map, ordered by a list of indices.
  Skip numbering if a question is marked as :skip."
  [question-map indices pos]
  (if (empty? indices)
    question-map
    (let [[index & next-indices] indices
          not-skip? (not (get-in question-map [index :skip]))
          next-map (cond->
                     question-map
                     not-skip?
                     (assoc-in [index :pos] pos))
          next-pos (cond-> pos not-skip? inc)]
      (recur next-map next-indices next-pos))))

(defn question-list-view
  "question-index holds the indexed data, while the order is determined by question-list.
  To obtain a list view of the questions, de-reference the index using the question list."
  [{:keys [question-map question-list ] :as state-info}]
  (when question-map
    (let [outlined-map (outline-pos question-map question-list 1)]
      (map outlined-map question-list))))

(defn item-list-view [item-map item-list]
  (->> item-list (map (partial get item-map)) (filter some?)))

(defn next-item-id [item-map]
  (->> item-map
       keys
       count
       str))

(defn get-new-question [state-info]
  (get-in state-info [:question-map :new-question]))

(defn add-question
  "Add a question to both the question index and list.
  To update the state, use this function with swap! ."
  [state-info question]
  (let [question-id (next-item-id (:question-map state-info))
        question-info (assoc question :index question-id)]
    (-> state-info
      (assoc-in [:question-map question-id] question-info)
      (update :question-list conj question-id)
      (assoc :edit-index question-id))))

;; TODO could refactor this - common logic with add-question
(defn add-answer-type
  "Add a question to both the question index and list.
  To update the state, use this function with swap! ."
  [state-info item]
  (let [answer-id (str (next-item-id (:answer-types state-info)))
        edit-question-id (:edit-index state-info)
        answer-info (assoc item :custom-index answer-id)]
    (-> state-info
      (assoc-in [:answer-types answer-id] answer-info)
      (assoc-in [:question-map edit-question-id :answer-type] answer-id))))

(defn add-coll-answers [answers {:keys [index] :as question-info}]
  (let [coll-answers (map #(get % (str index)) answers)]
    (assoc question-info :coll-answers coll-answers)))

(defn questions-with-coll-answers [answers question-list]
  (let [coll-fn (partial add-coll-answers (vals answers))]
    (map coll-fn question-list)))

(defn add-answer-keys [answer-types question-info]
  (let [answer-keys (get-in answer-types [(:answer-type question-info) :params :values])
        template (get-in answer-types [(:answer-type question-info) :template])]
    (assoc question-info :answer-keys answer-keys :template template)))

(defn questions-with-answer-keys [answer-types question-list]
  (let [add-keys-fn (partial add-answer-keys answer-types)]
    (map add-keys-fn question-list)))

(defn agg-values [values key]
  [key (count (filter #{key} (flatten values)))])

(defn add-answer-agg [{:keys [answer-keys coll-answers] :as question-info}]
  (let [agg-fn (partial agg-values coll-answers)
        answer-agg (into {} (map agg-fn answer-keys))]
    (assoc question-info :answer-agg answer-agg)))

(defn questions-with-agg-answers [question-list]
    (map add-answer-agg question-list))

(defn survey-result-agg [survey-info answers]
  (let [question-list (question-list-view survey-info)
        answer-types (:answer-types survey-info)
        aggregated  (->> question-list
                      (questions-with-answer-keys answer-types)
                      (questions-with-coll-answers answers)
                      (questions-with-agg-answers))]
    {:survey-info (select-keys survey-info [:surveyno :surveyname])
     :question-answer-agg aggregated}))

