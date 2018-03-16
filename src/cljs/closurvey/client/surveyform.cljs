(ns closurvey.client.surveyform
  (:require
    [clojure.string :as string]
    [closurvey.client.event :as event])) 

(defn question-list-view
  "question-index holds the indexed data, while the order is determined by question-list.
  To obtain a list view of the questions, de-reference the index using the question list." 
  [state-info]
  (map (:question-index state-info) (:question-list state-info)))

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
        (when required [:span.alert.alert-info.ml-1.pl-1.py-0.my-0.small "* Required"])]
      (when-let [answer-renderer (render-answer-type state-info answer-type)]
        (answer-renderer index question))])

