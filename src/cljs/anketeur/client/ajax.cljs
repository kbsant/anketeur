(ns anketeur.client.ajax
  (:require [ajax.core :as ajax]))

(defn local-uri? [{:keys [uri]}]
  (not (re-find #"^\w+?://" uri)))

(defn default-headers [js-context csrf-token request]
  (if (local-uri? request)
    (-> request
        (update :uri #(str js-context %))
        (update :headers #(merge {"x-csrf-token" csrf-token} %)))
    request))

(defn load-interceptors! [js-context csrf-token]
  (swap! ajax/default-interceptors
         conj
         (ajax/to-interceptor {:name "default headers"
                               :request (partial default-headers js-context csrf-token)})))


