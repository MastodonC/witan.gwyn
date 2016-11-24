(ns kixi.gwyn.data-test
  (:require  [clojure.test :refer :all]
             [clojure.java.io :as io]
             [clojure.data.csv :as data-csv]))

(defn open-csv [filename]
  (with-open [in-file (io/reader filename)]
    (vec (data-csv/read-csv in-file))))

(def forbidden-fields ["Motive"
                       "MainCause"
                       "ParentMainCause"
                       "MainCauseGroup"])

(deftest test-for-forbidden-fields
  (testing "csv's have forbidden fields present"
    (let [files (map (fn [x] (.getPath x)) (file-seq (io/file "data/")))
          csv-files (filter (fn [f] (re-find #"\.csv$" f)) files)]
      (is (empty? (if (not-empty csv-files)
                    (let [headers (map (comp first open-csv) csv-files)]
                      (mapcat (fn [x]
                                (filter (fn [z] ((set x) z)) forbidden-fields))
                              headers))))))))
