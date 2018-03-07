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

(defn read-transit-state [encoded-js-state state-keys]
  (let [transit-state (read-json (base64/decodeString encoded-js-state))]
    (select-keys transit-state state-keys)))

(defn anti-forgery-field [csrf-token]
  [:input.__anti-forgery-token 
    {:name "__anti-forgery-token"
     :type :hidden
     :value csrf-token}])
