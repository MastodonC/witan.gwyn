(ns kixi.gwyn.utils
  (:require [kixi.gwyn.test-utils :as tu]
            [clojure.string :as str]))

(defn make-coll [x]
  (cond
    (seq? x) x
    (vector? x) x
    :else (vector x)))

(defn safe-divide
  [d dd]
  (if (zero? dd)
    0.0
    (/ ^double d dd)))

(defn remove-punctuation
  [s]
  (str/replace s #"[-\\\/\(\)]" " "))

(defn remove-empty
  [coll]
  (keep not-empty coll))

(defn prepare-properties [filepath]
  (->> (-> filepath
           tu/load-csv
           :columns
           flatten)
       (map #(-> %
                 str/lower-case
                 remove-punctuation
                 str/trim
                 (str/split #" ")
                 remove-empty))))

(defn match-property-names
  []
  (let [lfb-properties (prepare-properties
                        "data/test_data/properties_lfb.csv")
        googleapi-properties (prepare-properties
                              "data/test_data/properties_googleapi.csv")]
    ;; (map (fn [lfb] (if (some  lfb)))
    ;;      lfb-properties)
    ))
