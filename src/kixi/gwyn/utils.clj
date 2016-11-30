(ns kixi.gwyn.utils
  (:require [kixi.gwyn.test-utils :as tu]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]))

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
  (str/replace s #" or | in " " "))

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

;; First try
;; Tried to match all elements in a LFB properties names to a google api name
;; The result is a LFB name then matches several names
;; It's not convenient to write to a csv and I lose track of the original names
(defn match-property-names [outpath]
  (let [lfb-properties (-> "data/test_data/properties_lfb.csv"
                           prepare-properties
                           prepare-lfb-data)
        googleapi-properties (prepare-properties
                              "data/test_data/properties_googleapi.csv")
        data (->> lfb-properties
                  (map #(for [g googleapi-properties]
                          (let [match (find-match % g)]
                            (when (not-empty match)
                              {% match}))))
                  (reduce concat)
                  (keep not-empty)
                  (reduce #(merge-with concat %1 %2))
                  (into [])
                  (conj [["LFB property names" "Googleapi property names"]]))]
    (with-open [out-file (io/writer outpath)]
      (csv/write-csv out-file data))))

(defn remove-deprecated
  [s]
  (str/replace s #" deprecated " " "))

(defn clean-lfb [s]
  (-> s
      str/lower-case
      remove-punctuation
      remove-stopwords
      str/trim))

(defn clean-ga [s]
  (-> s
      str/lower-case
      remove-punctuation
      remove-deprecated
      str/trim))

;; Second try
;; I'm trying to keep track of the original names (before cleaning)
;; Also here I'm trying to match the whole names (not breaking down LFB names)
;; And in the process of producing a data structure easy to write to a csv
(defn match-properties []
  (let [input-lfb (-> "data/test_data/properties_lfb.csv"
                      tu/load-csv
                      :columns)
        input-googleapi (-> "data/test_data/properties_googleapi.csv"
                            tu/load-csv
                            :columns)]
    (map (fn [v] (let [lfb-name (first v)
                       lfb-name-cleaned (clean-lfb lfb-name)]
                   (conj v
                         lfb-name-cleaned
                         (keep (fn [ga] (when (not-empty (re-matches
                                                         (re-pattern
                                                          (str ".*" lfb-name-cleaned ".*"))
                                                         (clean-ga ga)))
                                         ga))
                              input-googleapi))))
         input-lfb)))
