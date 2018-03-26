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

