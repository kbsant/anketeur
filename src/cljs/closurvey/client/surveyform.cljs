(ns closurvey.client.surveyform
  (:require
    [clojure.string :as string]
    [closurvey.client.event :as event]
    [closurvey.client.ui :as ui]))

(defn change-handler [state form-id index ev]
  (let [form (ui/element-by-id form-id)
        value (ui/form-element-value form index)]
    (swap! state assoc-in [:answers index] value)))

(defn with-change-handler [m handler index]
  (cond-> m
    handler (assoc :on-change #(handler index %))))

(defn render-template-radio-or-checkbox
  "Return an answer renderer function for a radio or checkbox."
  [radio-or-checkbox {:keys [values]}]
  (fn [index {:keys [allow-na]} handler]
    (let [input-name (str index)]
      [:div.row.ml-1.pl-1
        (map-indexed
          (fn [i input-value]
            ^{:key i}
            [:label.mr-1
              [:input.mr-1
                (with-change-handler
                  {:type radio-or-checkbox :name input-name :value input-value}
                  handler
                  input-name)]
              input-value])
          values)
        (when (and allow-na (= :radio radio-or-checkbox))
          [:label.mr-1
            [:input.mr-1
              (with-change-handler
                {:type :radio :name input-name :value "Not applicable"}
                handler
                input-name)]
            "Not applicable"])])))

(defn render-answer-text-area
  "Return an answer renderer function for a text area."
  [_]
  (fn [index _ handler]
    (let [input-name (str index)]
      [:div.row.ml-1.pl-1
        [:textarea
          (with-change-handler
            {:name input-name}
            handler
            input-name)]])))

(def answer-templates
  {:radio (partial render-template-radio-or-checkbox :radio)
   :checkbox (partial render-template-radio-or-checkbox :checkbox)
   :text-area render-answer-text-area})

(defn render-answer-type [state-info answer-type]
  (let [{:keys [template params]} (get-in state-info [:answer-types answer-type])
        render-fn (get answer-templates template)]
    (when render-fn
      (render-fn params))))

(defn render-question
  [state-info
   pos
   {:keys [index question-text answer-type required allow-na] :as question}
   handler]
  ^{:key index}
  [:div.row
      [:p
        [:span.mr-1.font-weight-bold (str (inc pos))]
        question-text
        (when required [:span.alert.alert-info.ml-1.pl-1.py-0.my-0.small "* Required"])]
      (when-let [answer-renderer (render-answer-type state-info answer-type)]
        (answer-renderer index question handler))])

;; pass the state-ref and state-info to avoid de-referencing a ratom,
;; since reagent deref has side-effects that aren't needed in this call.
(defn render-form-question [state-ref state-info form-id pos question]
  (render-question state-info pos question (partial change-handler state-ref form-id)))

(defn preview-question [state-info pos question]
  (render-question state-info pos question nil))

