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

(def fire-stations-data
  {:fire-station-lookup-table ["data/fire_station_data.csv" sc/FireStations]})

(def test-data
  (reduce merge (map (fn [[k [p s]]]
                       (hash-map k (tu/csv-to-dataset p s)))
                     data-info)))

(def fire-stations
  (reduce merge (map (fn [[k [p s]]]
                       (hash-map k (tu/csv-to-dataset p s)))
                     fire-stations-data)))

(deftest group-commercial-properties-type-test
  (testing "The function returns the properties data needed for score calc"
    (let [result (group-commercial-properties-type-1-0-0 test-data)
          result-data (:commercial-properties-by-type result)]
      (is (ds/dataset? result-data))
      (is (= (second (:shape result-data)) 4))
      (is (= (set (:column-names result-data))
             #{:property-type :num-fires :avg-pumps-attending :sd-pumps-attending})))))

(deftest extract-fire-station-geo-data-test
  (testing "function returns the data needed for Google Places API"
    (let [result (extract-fire-station-geo-data-1-0-0 fire-stations {:fire-station "Twickenham"})
          result-data (:fire-station-geo-data result)]
      (is (ds/dataset? result-data))
      (is (= (second result-data)) 3)
      (is (= (set (:column-names result-data))
             #{:radius :lat :long})))))
