(ns closurvey.model)

(defn question-list-view
  "question-index holds the indexed data, while the order is determined by question-list.
  To obtain a list view of the questions, de-reference the index using the question list."
  [{:keys [question-map question-list ] :as state-info}]
  (when question-map
    (map question-map question-list)))

(defn next-question-id [question-map]
  (->> question-map
       keys
       count))

(defn add-question
  "Add a question to both the question index and list.
  To update the state, use this function with swap! ."
  [state-info question]
  (let [question-id (next-question-id (:question-map state-info))
        question-info (assoc question :index question-id)]
    (-> state-info
      (assoc-in [:question-map question-id] question-info)
      (update :question-list conj question-id))))

(defn add-coll-answers [answers {:keys [index] :as question-info}]
  (let [coll-answers (map #(get % (str index)) answers)]
    (assoc question-info :coll-answers coll-answers)))

(defn questions-with-coll-answers [answers question-list]
  (let [coll-fn (partial add-coll-answers (vals answers))]
    (map coll-fn question-list)))

(defn add-answer-keys [answer-types question-info]
  (let [answer-keys (get-in answer-types [(:answer-type question-info) :params :values])]
    (assoc question-info :answer-keys answer-keys)))

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

