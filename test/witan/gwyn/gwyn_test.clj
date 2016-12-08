(ns witan.gwyn.gwyn-test
  (:require [clojure.test :refer :all]
            [witan.gwyn.gwyn :refer :all]
            [witan.gwyn.schemas :as sc]
            [schema.core :as s]
            [witan.gwyn.test-utils :as tu]
            [witan.gwyn.utils :as u]
            [witan.datasets :as wds]
            [clojure.core.matrix.dataset :as ds]
            [clojure.set :as clj-set]))

(def data-info
  {:lfb-historic-incidents ["data/lfb_historical_fire_non_residential.csv"
                            sc/LfbHistoricIncidents]})

(def fire-stations-data
  {:fire-station-lookup-table ["data/fire_station_data.csv" sc/FireStations]})

(def property-comparison-data
  {:property-comparison ["data/lfb_to_google.csv" sc/PropertyComparison]})

(def historical-fire-risk-scores
  {:historical-fire-risk-scores ["data/template-historical-fire-risk-scores.csv" sc/HistoricalFireRiskScores]})

(def test-data
  (tu/to-dataset data-info))

(def fire-stations
  (tu/to-dataset fire-stations-data))

(def property-comparison
  (tu/to-dataset property-comparison-data))

(def foo-location (hash-map :fire-station-geo-data
                            (ds/dataset [{:radius 1000.1 :lat 51.43444023 :long -0.346214694}])))

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

(deftest list-commercial-properties-test
  (testing "function returns properties in the first station's area"
    (let [result (list-commercial-properties-1-0-0 foo-location)
          result-data (:commercial-properties result)
          all-types (set (wds/subset-ds result-data :cols :type))]
      (is (ds/dataset? result-data))
      (is (= (set (:column-names result-data))
             #{:address :name :type}))
      (is (every? empty?
                  (map #(clj-set/intersection % (set u/unwanted-property-types))
                       all-types)))
      (is (not (contains? all-types #{}))))))

(deftest associate-risk-score-to-commercial-properties-test
  (testing "function returns properties with added cols :risk-score & :date-last-risk-assessed"
    (let [result (associate-risk-score-to-commercial-properties-1-0-0
                  (merge (-> test-data
                             group-commercial-properties-type-1-0-0
                             generic-commercial-properties-fire-risk-1-0-0)
                         (-> foo-location
                             list-commercial-properties-1-0-0)
                         property-comparison))
          result-data (:commercial-properties-with-scores result)]
      (is (ds/dataset? result-data))
      (is (= (set (:column-names result-data))
             #{:address :name :risk-score :date-last-risk-assessed})))))
