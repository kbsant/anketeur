(ns anketeur.client.ui
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [clojure.core.async :as async :refer [>! <!]]
    [clojure.string :as string]
    [cognitect.transit :as t]))

(def json-reader (t/reader :json))

(def json-writer (t/writer :json))

(defn read-json [json]
  (t/read json-reader json))

(defn write-json [value]
  (t/write json-writer value))

(defn target-value [ev]
  (-> ev .-target .-value))

(defn js-assoc [key ev data]
  (assoc data key (target-value ev)))

(defn js-assoc-in [keys ev data]
  (assoc-in data keys (target-value ev)))

(defn js-update [key f ev data]
  (update data key f (target-value ev)))

(defn js-update-in [keys f ev data]
  (update-in data keys f (target-value ev)))

(defn js-assoc-fn [key ev]
  (partial js-assoc key ev))

(defn js-assoc-in-fn [key ev]
  (partial js-assoc-in key ev))

(defn js-update-fn [keys f ev]
  (partial js-update keys f ev))

(defn js-update-in-fn [keys f ev]
  (partial js-update-in keys f ev))

(defn element-by-id [id]
  (.getElementById js/document id))

(defn form-element-value [form name]
  (let [parent (-> form .-elements (.namedItem name))
        length (or (some-> (.-length parent) (> 1)) 0)
        parent-seq (array-seq parent)
        child-type (when (> length 0) (.-type (first parent-seq)))]
    (if (= "checkbox" child-type)
      (->> parent-seq (filter #(.-checked %)) (map #(.-value %)))
      (.-value parent))))

(defn anti-forgery-field [csrf-token]
  [:input.__anti-forgery-token
    {:name "__anti-forgery-token"
     :type :hidden
     :value csrf-token}])

(defn render-selector-row
  [link-fn i {:keys [surveyname] :as survey-info}]
  ^{:key i}
  [:li
    [:a
      {:href (link-fn survey-info)}
      (or surveyname [:span.label.label-default "(no name)"])]])

(defn doc-selector [link-fn {:keys [doclist]}]
  (let [row-renderer (partial render-selector-row link-fn)]
    (when-not (empty? doclist)
      [:ul
        (map-indexed row-renderer doclist)])))

(defn errors-div [errors-key state]
  (when-let [errors (get @state errors-key)]
    [:div
      (->> errors
        (map-indexed
          (fn [i error]
            ^{:key i}
            [:p.text-danger error])))]))

(defn fade-opacity [check-string]
   (if (string/blank? check-string)
      {:opacity 1}
      {:opacity 0 :transition [:opacity "3s"]}))

(defn repeat-timer [timer-fn millis]
  (go-loop []
    (<! (async/timeout millis))
    (timer-fn)
    (recur)))

(defn single-timer[timer-fn millis]
   (go
      (<! (async/timeout millis))
      (timer-fn)))

