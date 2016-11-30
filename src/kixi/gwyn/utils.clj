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
  (str/replace s #"[-\\\/\(\)_]" " "))

(defn remove-empty
  [coll]
  (keep not-empty coll))

(defn remove-stopwords
  [s]
  (str/replace s #" or | in " ""))

(defn prepare-properties [filepath]
  (->> (-> filepath
           tu/load-csv
           :columns
           flatten)
       (map #(-> %
                 str/lower-case
                 remove-punctuation
                 str/trim))))

(defn prepare-lfb-data
  [coll]
  (map #(-> % remove-stopwords (str/split #" ") remove-empty) coll))

(defn find-match
  "Takes as an input a collection of split lfb property names
   (ie ('bus' 'coach' 'station' 'garage')) and a string of googleapi
   names (ie 'bus station') and look for matches between the terms of
   the collection and the googleapi names."
  [lfb-coll googleapi-str]
  (keep (fn [s] (re-matches (re-pattern (str ".*" s ".*")) googleapi-str))
        lfb-coll))

(defn match-property-names
  []
  (let [lfb-properties (-> "data/test_data/properties_lfb.csv"
                           prepare-properties
                           prepare-lfb-data)
        googleapi-properties (prepare-properties
                              "data/test_data/properties_googleapi.csv")]
    (->> lfb-properties
         (map #(for [g googleapi-properties]
                 (let [match (find-match % g)]
                   (when (not-empty match)
                     {% (first match)}))))
         (reduce concat)
         (keep not-empty))))
