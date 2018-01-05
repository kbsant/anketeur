(ns closurvey.util.json
  (:refer-clojure :exclude [read])
  (:require [cognitect.transit :as transit])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

(defn bytes-output [] (ByteArrayOutputStream. 2048))
(defn stream-reader [stream] (transit/reader stream :json))
(defn stream-writer [stream] (transit/writer stream :json))
(defn bytes-reader [bytes] (stream-reader (ByteArrayInputStream. bytes)))
(defn utf8-reader [str] (bytes-reader (.getBytes str "utf-8")))
(defn read [reader] (transit/read reader))
(defn write [writer data] (transit/write writer data))
(defn read-utf8 [str] (read (utf8-reader str)))
(defn write-utf8 [data]
  (let [out (bytes-output)
        writer (stream-writer out)]
    (write writer data)
    (.toString out "utf-8")))
