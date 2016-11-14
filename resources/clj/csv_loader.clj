(ns csv-loader
  (:require [clojure.data.csv :as data-csv]
            [clojure.java.io :as io]))

(defn open-csv [filename]
  (with-open [in-file (io/reader filename)]
    (into []
          (data-csv/read-csv in-file))))

(defn load-csv [filename]
  "takes a filename of a csv and returns a seq of maps with
   headers as keys and rows as vals"
  (let [[header & file-data] (open-csv filename)
        hk (mapv keyword header)]
    (map #(zipmap hk %) file-data)))
