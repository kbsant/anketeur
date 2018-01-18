(ns closurvey.client.event)

(defn assoc-with-js-value [state key]
  (fn [ev]
    (swap! state assoc key (-> ev .-target .-value))))

(defn assoc-in-with-js-value [state keys]
  (fn [ev]
    (swap! state assoc-in keys (-> ev .-target .-value))))

(defn update-with-js-value [state key fun]
  (fn [ev]
    (swap! state update key fun (-> ev .-target .-value))))

(defn update-in-with-js-value [state keys fun]
  (fn [ev]
    (swap! state update keys fun (-> ev .-target .-value))))

