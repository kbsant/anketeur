(ns anketeur.form
  (:require
    [clojure.string :as string]))

(defn navbar [contents]
  [:div.container.navbar contents])

(defn errors-div [errors]
  (when errors
    [:div
      (->> errors
        (map-indexed
          (fn [i error]
            ^{:key i}
            [:p.text-danger error])))]))

(defn anti-forgery-field [csrf-token]
  [:input.__anti-forgery-token
    {:name "__anti-forgery-token"
     :type :hidden
     :value csrf-token}])

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
                  index)]
              input-value])
          values)
        (when (and allow-na (= :radio radio-or-checkbox))
          [:label.mr-1
            [:input.mr-1
              (with-change-handler
                {:type :radio :name input-name :value "Not applicable"}
                handler
                index)]
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
            index)]])))

(def answer-templates
  {:radio (partial render-template-radio-or-checkbox :radio)
   :checkbox (partial render-template-radio-or-checkbox :checkbox)
   :text-area render-answer-text-area})

(defn render-answer-type
  ([state-info answer-type]
   (render-answer-type (get-in state-info [:answer-types answer-type])))
  ([{:keys [template params]}]
   (when-let [render-fn (get answer-templates template)]
     (render-fn params))))

(defn render-question
  [state-info
   {:keys [index pos question-text answer-type required allow-na] :as question}
   handler]
  ^{:key (str "q." index)}
  [:div.row
      [:p
        [:span.mr-1.font-weight-bold (str pos)]
        question-text
        (when required [:span.alert.alert-info.ml-1.pl-1.py-0.my-0.small "* Required"])]
      (when-let [answer-renderer (render-answer-type state-info answer-type)]
        (answer-renderer index question handler))])

(defn preview-question [state-info question]
  (render-question state-info question nil))

