(ns closurvey.filestore
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]))

;; File system with a write-behind cache
;;
;; This is a lightweight db useful for prototypes. 
;; TODO need to queue write operations to avoid race conditions
;; A write operation writes to the cache and schedules a write to the fs.
;; A read operation reads from the cache
;; 

(defn make-path [& elements]
  (clojure.string/join java.io.File/separator elements))

(defn last-childname
  "Get the current max name in the base directory"
  [base-dir]
  (->> (io/file base-dir)
    (.listFiles)
    (map #(.getName %))
    (sort)
    (last)))

(defn format-name [num] (format "%08d" num))

(defn parse-name [name] (Integer/parseInt name))

(defn assoc-unique [m k v]
 (if-not (contains? m k) (assoc m k v) m)) 

;; FIXME this isnt thread safe -- after the prototype, move to a db
(defn next-childname
  "Generate the next name after the max name in the base directory, or a new name"
  [base-dir]
  (or
    (some->> (last-childname base-dir)
      (parse-name)
      (inc)
      (format-name))
    (format-name 1)))

(defn init-store!
  "Make base dir and return immutable info about the store (base dir and table name)" 
  [base-dir table]
  (let [table-base (make-path base-dir table)] 
    (-> (io/file table-base) .mkdirs) 
    {:base-dir base-dir
     :table table
     :table-base table-base}))

(defn child-path [store child-fn]
  (let [table-base (:table-base store)
        child-name (child-fn table-base)]
    (when child-name
      (make-path table-base child-name))))

(defn last-child-path [store]
  (child-path store last-childname))

(defn next-child-path [store]
  (child-path store next-childname))

(defn read-edn-file [filename]
  (when filename
    (-> (slurp filename) edn/read-string)))

(defn write-edn-file! [filename data]
  (when data
    (spit filename (prn-str data))))

(defn read-store [store]
  (read-edn-file (last-child-path store)))

(defn write-store! [store data]
  (write-edn-file! (next-child-path store) data))

(defn read-table [base-dir table-name]
  (let [store (init-store! base-dir table-name)
        data (read-store store)]
    {:store-info store
     :data (atom (or data {}))}))
    
;; TODO enqueue write tasks on a channel
(defn write-table[table]
  (let [{:keys [store-info data]} table]
    (write-store! store-info @data)))



