(ns kixi.gwyn.gwyn-test
  (:require [clojure.test :refer :all]
            [kixi.gwyn.gwyn :refer :all]
            [kixi.gwyn.schemas :as sc]
            [schema.core :as s]
            [kixi.gwyn.test-utils :as tu]
            [witan.datasets :as wds]
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

(deftest adjust-avg-num-pumps-test
  (testing "The function adjust average pumps numbers"
    (let [properties-data (:commercial-properties-by-type
                           (group-commercial-properties-type-1-0-0 test-data))
          adjusted-data (adjust-avg-num-pumps properties-data)]
      (is (ds/dataset? adjusted-data))
      (is (= (second (:shape adjusted-data)) 4))
      (is (= (set (:column-names adjusted-data))
             #{:property-type :num-fires :adjusted-avg-pumps :sd-pumps-attending})))))

(deftest sort-by-pumps-and-fires-test
  (testing "The function sort by pumps average and by number of fires"
    (let [adjusted-data (adjust-avg-num-pumps
                         (:commercial-properties-by-type
                          (group-commercial-properties-type-1-0-0 test-data)))
          sorted-data (sort-by-pumps-and-fires adjusted-data)]
      (is (ds/dataset? sorted-data))
      (is (= (second (:shape sorted-data)) 4))
      (is (= (set (:column-names sorted-data))
             #{:property-type :num-fires :adjusted-avg-pumps :sd-pumps-attending}))
      (is (< (wds/subset-ds sorted-data :cols :adjusted-avg-pumps :rows 0)
             (wds/subset-ds sorted-data :cols :adjusted-avg-pumps :rows
                            (dec (first (:shape sorted-data))))))
      (is (< (wds/subset-ds sorted-data :cols :num-fires :rows 0)
             (wds/subset-ds sorted-data :cols :num-fires :rows
                            (dec (first (:shape sorted-data)))))))))

(deftest assign-generic-scores-test
  (testing "The function assign scores based on row's rank"
    (let [sorted-data (-> test-data
                          group-commercial-properties-type-1-0-0
                          :commercial-properties-by-type
                          adjust-avg-num-pumps
                          sort-by-pumps-and-fires)
          added-scores (assign-generic-scores sorted-data)]
      (println added-scores)
      (is (ds/dataset? added-scores))
      (is (= (second (:shape added-scores)) 2))
      (is (= (set (:column-names added-scores))
             #{:property-type :generic-fire-risk-score})))))

(deftest generic-commercial-properties-fire-risk-test
  (testing "The creation of the generic fire risk scores"
    (let [ds-scores (-> test-data
                        group-commercial-properties-type-1-0-0
                        generic-commercial-properties-fire-risk-1-0-0
                        :generic-fire-risks)
          scores (wds/subset-ds ds-scores :cols :generic-fire-risk-score)]
      (is (> (wds/subset-ds ds-scores
                            :cols :generic-fire-risk-score
                            :rows (dec (first (:shape ds-scores))))
             (wds/subset-ds ds-scores
                            :cols :generic-fire-risk-score
                            :rows 0)))
      (is (= (apply max scores) (wds/subset-ds ds-scores
                                               :cols :generic-fire-risk-score
                                               :rows (dec (first (:shape ds-scores))))))
      (is (= (apply min scores) (wds/subset-ds ds-scores
                                               :cols :generic-fire-risk-score
                                               :rows 0))))))
