(ns anketeur.client.event)

(defn target-value [ev]
  (-> ev .-target .-value))

(defn assoc-with-js-value [state key]
  (fn [ev]
    (swap! state assoc key (target-value ev))))

(defn assoc-in-with-js-value [state keys]
  (fn [ev]
    (swap! state assoc-in keys (target-value ev))))

(defn update-with-js-value [state key fun]
  (fn [ev]
    (swap! state update key fun (target-value ev))))

(defn update-in-with-js-value [state keys fun]
  (fn [ev]
    (swap! state update-in keys fun (target-value ev))))

