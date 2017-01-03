(ns witan.gwyn.acceptance.workspace-test
  (:require [clojure.test :refer :all]
            [witan.gwyn.gwyn :refer :all]
            [schema.core :as s]
            [witan.gwyn.schemas :as sc]
            [witan.gwyn.model :as m]
            [witan.workspace-api.protocols :as p]
            [witan.workspace-executor.core :as wex]
            [witan.gwyn.test-utils :as tu]
            [clojure.core.matrix.dataset :as ds]))

(def test-inputs
  {:fire-stations-lookup ["data/fire_station_data.csv"
                          sc/FireStations]
   :lfb-historic-incidents ["data/lfb_historical_fire_non_residential.csv"
                            sc/LfbHistoricIncidents]
   :historical-fire-risk-scores ["data/template-historical-fire-risk-scores.csv"
                                 sc/HistoricalFireRiskScores]
   :property-comparison ["data/lfb_to_google.csv"
                         sc/PropertyComparison]
   :fire-station "Twickenham"})

(def stations (map first (:columns (tu/load-csv (first (:fire-stations-lookup test-inputs))))))

(defn param-input [area]
  (assoc test-inputs :fire-station area))

(defn read-inputs [data input _ schema]
  (let [[filepath fileschema] (get data (:witan/name input))]
    (tu/csv-to-dataset filepath fileschema)))

(defn add-input-params
  [input]
  (assoc-in input [:witan/params :fn] (partial read-inputs test-inputs input)))

(deftest gwyn-workspace-test
  (testing "The model is run on the workspace and returns the outputs expected"
    (let [fixed-catalog (mapv #(if (= (:witan/type %) :input) (add-input-params %) %)
                              (:catalog m/gwyn-model))
          workspace     {:workflow  (:workflow m/gwyn-model)
                         :catalog   fixed-catalog
                         :contracts (p/available-fns (m/model-library))}
          workspace'    (s/with-fn-validation (wex/build! workspace))
          result        (apply merge (wex/run!! workspace' {}))]
      ;;(clojure.pprint/pprint (map :address (ds/row-maps (:new-fire-risk-scores result))))
      (is result)
      (is (:new-fire-risk-scores result)))))

(defn run-in-repl [input]
  (let [fixed-catalog (mapv #(if (= (:witan/type %) :input)
                               (add-input-params %)
                               (if (get-in % [:witan/params :fire-station])
                                 (assoc-in % [:witan/params :fire-station] (:fire-station input))
                                 %))
                            (:catalog m/gwyn-model))
        workspace     {:workflow  (:workflow m/gwyn-model)
                       :catalog   fixed-catalog
                       :contracts (p/available-fns (m/model-library))}
        workspace'    (s/with-fn-validation (wex/build! workspace))]
    (apply merge (wex/run!! workspace' {}))))
