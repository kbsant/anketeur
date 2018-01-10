(ns closurvey.client.event)

(defn assoc-with-js-value [state key]
  (fn [ev]
    (swap! state assoc key (-> ev .-target .-value))))

(defn update-with-js-value [state key fun]
  (fn [ev]
    (swap! state update key fun (-> ev .-target .-value))))


