(ns closurvey.client.ui
  (:require
    [goog.crypt.base64 :as base64]
    [cognitect.transit :as t]))

(def json-reader (t/reader :json))

(def json-writer (t/writer :json))

(defn read-json [json]
  (t/read json-reader json))

(defn write-json [value]
  (t/write json-writer value))

(defn read-transit-state
  ([encoded-js-state]
   (read-transit-state encoded-js-state nil)) 
  ([encoded-js-state state-keys]
   (let [transit-state (read-json (base64/decodeString encoded-js-state))]
      (if state-keys
        (select-keys transit-state state-keys)
        transit-state))))

(defn anti-forgery-field [csrf-token]
  [:input.__anti-forgery-token 
    {:name "__anti-forgery-token"
     :type :hidden
     :value csrf-token}])
