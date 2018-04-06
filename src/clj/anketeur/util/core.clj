(ns anketeur.util.core
  (:require
    [cheshire.core :as json]))

(defmulti export-format (fn [format _] format))

(defmethod export-format "CSV" [_ data]
  (json/generate-string data {:pretty true}))

(defmethod export-format "JSON" [_ data]
  (json/generate-string data {:pretty true}))

(defmethod export-format "EDN" [_ data]
  (clojure.pprint/write data :stream nil))

(defn resource-edn [path]
  (-> path clojure.java.io/resource slurp clojure.edn/read-string))
