(ns kixi.gwyn.gwyn
  (:require [witan.workspace-api :refer [defworkflowfn
                                         definput
                                         defworkflowoutput]]
            [schema.core :as s]
            [kixi.gwyn.schemas :as sc]
            [clojure.core.matrix.dataset :as ds]
            [witan.datasets :as wds]
            [witan.datasets.stats :as wst]
            [kixi.gwyn.utils :as u]
            [kixi.stats.core :refer [mean standard-deviation]]))

(definput fire-station-lookup-table-1-0-0
  {:witan/name :fire-risk/fire-station-lookup-table
   :witan/version "1.0.0"
   :witan/key :fire-station-lookup-table
   :witan/schema sc/FireStations})

(definput lfb-historic-incidents-1-0-0
  {:witan/name :fire-risk/lfb-historic-incidents
   :witan/version "1.0.0"
   :witan/key :lfb-historic-incidents
   :witan/schema sc/LfbHistoricIncidents})

(definput historical-fire-risk-scores-1-0-0
  {:witan/name :fire-risk/historical-fire-risk-scores
   :witan/version "1.0.0"
   :witan/key :historical-fire-risk-scores
   :witan/schema sc/HistoricalFireRiskScores})

(defworkflowfn extract-fire-station-geo-data-1-0-0
  {:witan/name :fire-risk/extract-fire-station-geo-data
   :witan/version "1.0.0"
   :witan/input-schema {:fire-station-lookup-table sc/FireStations}
   :witan/param-schema {:fire-station s/Str}
   :witan/output-schema {:fire-station-geo-data sc/FireStationGeoData}}
  [{:keys [fire-station-lookup-table]} {:keys [fire-station]}]
  {:fire-station-geo-data
   (-> fire-station-lookup-table
       (wds/select-from-ds {:station fire-station})
       (wds/subset-ds :cols [:radius :lat :long]))})
(defworkflowfn list-commercial-properties-1-0-0
  {:witan/name :fire-risk/list-commercial-properties
   :witan/version "1.0.0"
   :witan/input-schema {:fire-station-geo-data sc/FireStationGeoData}
   :witan/output-schema {:commercial-properties sc/CommercialProperties}}
  [{:keys [fire-station-geo-data]} _]
  {:commercial-properties {}})

(defworkflowfn group-commercial-properties-type-1-0-0
  "Takes in LFB historical incidents data for fires in non-residential properties.
   Returns the number of fires, average and standard deviation for the number of pumps
   attending the fire at that type of non-residential property."
  {:witan/name :fire-risk/group-commercial-properties-type
   :witan/version "1.0.0"
   :witan/input-schema {:lfb-historic-incidents sc/LfbHistoricIncidents}
   :witan/output-schema {:commercial-properties-by-type sc/CommercialProperties}}
  [{:keys [lfb-historic-incidents]} _]
  {:commercial-properties-by-type
   (->> (wds/group-ds lfb-historic-incidents :property-type)
        (mapv (fn [[map-key ds]]
                (let [n (first (:shape ds))
                      avg (fn [coll] (u/safe-divide (apply + coll) n))
                      coll-pumps-attending (u/make-coll
                                            (wds/subset-ds ds :cols :num-pumps-attending))]
                  (merge map-key
                         {:num-fires n
                          :avg-pumps-attending
                          (avg coll-pumps-attending)
                          :sd-pumps-attending (wst/standard-deviation
                                               coll-pumps-attending)}))))
        ds/dataset)})

(defn adjust-avg-num-pumps
  "Takes in the properties dataset. Adjust the average number of pumps
   attending the fire depending on the value of the standard deviation."
  [properties-data]
  (-> properties-data
      (wds/add-derived-column :adjusted-avg-pumps [:sd-pumps-attending :avg-pumps-attending]
                              (fn [sd avg] (if (> sd 2) ;; value to be refined
                                             (+ avg (u/safe-divide sd 2))
                                             avg)))
      (ds/select-columns [:property-type :num-fires :adjusted-avg-pumps :sd-pumps-attending])))

(defn sort-by-pumps-and-fires
  "Takes in the properties dataset with the average number of pumps adjusted.
   Sort the property types by average number of pumps and by number of fires."
  [adjusted-properties-data]
  (->> (ds/row-maps adjusted-properties-data)
       (sort-by (juxt :adjusted-avg-pumps :num-fires))
       ds/dataset))

(defn assign-generic-scores
  "Takes in the properties dataset sorted by pumps and fires.
   Use the sorting order to assign a score to each property type."
  [sorted-properties-data]
  (let [range-data (range 1 (-> sorted-properties-data :shape first inc))]
    (-> sorted-properties-data
        (ds/add-column :generic-fire-risk-score range-data)
        (ds/select-columns [:property-type :generic-fire-risk-score]))))

(defworkflowfn generic-commercial-properties-fire-risk-1-0-0
  "Takes in a dataset with number of fires, average and standard deviation for the
   number of pumps attending the fire for each type of non-residential property.
   Returns a fire risk score for each type of non-residential property."
  {:witan/name :fire-risk/generic-commercial-properties-fire-risk
   :witan/version "1.0.0"
   :witan/input-schema {:commercial-properties-by-type sc/CommercialProperties}
   :witan/output-schema {:generic-fire-risks sc/GenericFireRisk}}
  [{:keys [commercial-properties-by-type]} _]
  {:generic-fire-risks (-> commercial-properties-by-type
                           adjust-avg-num-pumps
                           sort-by-pumps-and-fires
                           assign-generic-scores)})

(defworkflowfn associate-risk-score-to-commercial-properties-1-0-0
  {:witan/name :fire-risk/associate-risk-score-to-commercial-properties
   :witan/version "1.0.0"
   :witan/input-schema {:generic-fire-risks sc/GenericFireRisk
                        :commercial-properties sc/CommercialProperties}
   :witan/output-schema {:commercial-properties-with-scores sc/CommercialPropertiesWithScores}}
  [{:keys [generic-fire-risks commercial-properties]} _]
  {:commercial-properties-with-scores {}})

(defworkflowfn join-historical-and-new-scores-1-0-0
  {:witan/name :fire-risk/join-historical-and-new-scores
   :witan/version "1.0.0"
   :witan/input-schema {:commercial-properties-with-scores sc/CommercialPropertiesWithScores
                        :historical-fire-risk-scores sc/HistoricalFireRiskScores}
   :witan/output-schema {:historical-and-new-scores sc/HistoricalFireRiskScores}}
  [{:keys [commercial-properties-with-scores historical-fire-risk-scores]} _]
  {:historical-and-new-scores {}})

(defworkflowfn update-score-1-0-0
  {:witan/name :fire-risk/update-score
   :witan/version "1.0.0"
   :witan/input-schema {:historical-and-new-scores sc/HistoricalFireRiskScores}
   :witan/output-schema {:new-fire-risk-scores sc/NewFireRiskScores}}
  [{:keys [historical-and-new-scores]} _]
  {:new-fire-risk-scores {}})

(defworkflowoutput output-new-fire-risk-scores-1-0-0
  {:witan/name :fire-risk/output-new-fire-risk-scores
   :witan/version "1.0.0"
   :witan/input-schema {:new-fire-risk-scores sc/NewFireRiskScores}}
  [{:keys [new-fire-risk-scores]} _]
  {:new-fire-risk-scores new-fire-risk-scores})
