(ns kixi.gwyn.gwyn-test
  (:require [clojure.test :refer :all]
            [kixi.gwyn.gwyn :refer :all]
            [kixi.gwyn.schemas :as sc]
            [schema.core :as s]
            [kixi.gwyn.test-utils :as tu]))

(def LfbHistoricIncidentsSchema
  (sc/make-ordered-ds-schema [[:property-type s/Str]
                              [:num-stations-with-pumps-attending s/Int]
                              [:num-pumps-attending s/Int]]))

(def data-info
  {:lfb-historic-incidents ["data/lfb_historical_fire_non_residential.csv"
                            LfbHistoricIncidentsSchema]})

(def test-data
  (reduce merge (map (fn [[k [p s]]]
                       (hash-map k (tu/csv-to-dataset p s)))
                     data-info)))
