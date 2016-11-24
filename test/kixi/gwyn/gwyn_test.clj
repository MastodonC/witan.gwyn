(ns kixi.gwyn.gwyn-test
  (:require [clojure.test :refer :all]
            [kixi.gwyn.gwyn :refer :all]
            [kixi.gwyn.schemas :as sc]
            [schema.core :as s]
            [kixi.gwyn.test-utils :as tu]
            [clojure.core.matrix.dataset :as ds]))

(def data-info
  {:lfb-historic-incidents ["data/lfb_historical_fire_non_residential.csv"
                            sc/LfbHistoricIncidents]})

(def test-data
  (reduce merge (map (fn [[k [p s]]]
                       (hash-map k (tu/csv-to-dataset p s)))
                     data-info)))

(deftest group-commercial-properties-type-test
  (testing "The function returns the properties data needed for score calc"
    (let [result (group-commercial-properties-type-1-0-0 test-data)
          result-data (:commercial-properties-by-type result)]
      (is (ds/dataset? result-data))
      (is (= (second (:shape result-data)) 4))
      (is (= (set (:column-names result-data))
             #{:property-type :num-fires :avg-pumps-attending :sd-pumps-attending})))))
