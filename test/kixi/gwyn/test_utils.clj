(ns kixi.gwyn.test-utils
  (:require  [clojure.data.csv :as data-csv]
             [clojure.java.io :as io]
             [schema.coerce :as coerce]
             [kixi.gwyn.schemas :as sc]
             [schema.core :as s]
             [clojure.core.matrix.dataset :as ds]))

(defn load-csv
  "Loads csv file with each row as a vector.
   Stored in map separating column-names from data"
  ([filename]
   (let [file (io/file filename)]
     (when (.exists (io/as-file file))
       (let [parsed-csv (with-open [in-file (io/reader file)]
                          (doall (data-csv/read-csv in-file)))
             parsed-data (rest parsed-csv)
             rm-nil-values (filter (fn [d] (not-any? #(= "NULL" %) d)) parsed-data)
             headers (first parsed-csv)]
         {:column-names headers
          :columns (vec rm-nil-values)})))))


(defn apply-row-schema
  [col-schema csv-data]
  (let [row-schema (sc/make-row-schema col-schema)]
    (map (coerce/coercer row-schema coerce/string-coercion-matcher)
         (:columns csv-data))))

(defn apply-col-names-schema
  [col-schema csv-data]
  (let [col-names-schema (sc/make-col-names-schema col-schema)]
    ((coerce/coercer col-names-schema coerce/string-coercion-matcher)
     (:column-names csv-data))))

(defn apply-schema-coercion [data schema]
  {:column-names (apply-col-names-schema schema data)
   :columns (vec (apply-row-schema schema data))})

(defn csv-to-dataset
  "Takes in a file path and a schema. Creates a dataset with the file
   data after coercing it using the schema."
  [filepath schema]
  (-> (load-csv filepath)
      (apply-schema-coercion schema)
      (as-> {:keys [column-names columns]} (ds/dataset column-names columns))))
