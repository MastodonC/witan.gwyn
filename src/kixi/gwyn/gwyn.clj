(ns kixi.gwyn.gwyn
  (:require [witan.workspace-api :refer [defworkflowfn
                                         definput
                                         defworkflowoutput]]
            [schema.core :as s]
            [kixi.gwyn.schemas :as sc]))

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
  {:fire-station-geo-data {}})

(defworkflowfn list-commercial-properties-1-0-0
  {:witan/name :fire-risk/list-commercial-properties
   :witan/version "1.0.0"
   :witan/input-schema {:fire-station-geo-data sc/FireStationGeoData}
   :witan/output-schema {:commercial-properties sc/CommercialProperties}}
  [{:keys [fire-station-geo-data]} _]
  {:commercial-properties {}})

(defworkflowfn filter-by-commercial-properties-type-1-0-0
  {:witan/name :fire-risk/filter-by-commercial-properties-type
   :witan/version "1.0.0"
   :witan/input-schema {:lfb-historic-incidents sc/LfbHistoricIncidents}
   :witan/output-schema {:commercial-properties-by-type sc/CommercialProperties}}
  [{:keys [lfb-historic-incidents]} _]
  {:commercial-properties-by-type {}})

(defworkflowfn generic-commercial-properties-fire-risk-1-0-0
  {:witan/name :fire-risk/generic-commercial-properties-fire-risk
   :witan/version "1.0.0"
   :witan/input-schema {:commercial-properties-by-type sc/CommercialProperties}
   :witan/output-schema {:generic-fire-risks sc/GenericFireRisk}}
  [{:keys [commercial-properties-by-type]} _]
  {:generic-fire-risks {}})

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

(defworkflowfn update-score-with-priority-1-0-0
  {:witan/name :fire-risk/update-score-with-priority
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
