(ns witan.gwyn.model
  (:require [witan.workspace-api :refer [defmodel]]
            [witan.workspace-api.protocols :as p]
            [witan.workspace-api.utils :refer [map-fn-meta
                                               map-model-meta]]
            [witan.gwyn.gwyn :as g]))

(def gwyn-model-workflow
  "Define each step of the model"
  [[:fire-stations-lookup :extract-fire-station-geo-data]
   [:extract-fire-station-geo-data :list-commercial-properties]
   [:lfb-historic-incidents :group-commercial-properties-type]
   [:group-commercial-properties-type :generic-commercial-properties-fire-risk]
   [:property-comparison :associate-risk-score-to-commercial-properties]
   [:generic-commercial-properties-fire-risk :associate-risk-score-to-commercial-properties]
   [:list-commercial-properties :associate-risk-score-to-commercial-properties]
   [:associate-risk-score-to-commercial-properties :join-historical-and-new-scores]
   [:historical-fire-risk-scores :join-historical-and-new-scores]
   [:join-historical-and-new-scores :update-score]
   [:update-score :output-new-fire-risk-scores]])

(def gwyn-model-catalog
  "Provides metadata for each step in the model"
  [;; Input functions
   {:witan/name :fire-stations-lookup
    :witan/version "1.0.0"
    :witan/type :input
    :witan/fn :fire-risk/fire-station-lookup-table
    :witan/params {:src ""}}
   {:witan/name :lfb-historic-incidents
    :witan/version "1.0.0"
    :witan/type :input
    :witan/fn :fire-risk/lfb-historic-incidents
    :witan/params {:src ""}}
   {:witan/name :historical-fire-risk-scores
    :witan/version "1.0.0"
    :witan/type :input
    :witan/fn :fire-risk/historical-fire-risk-scores
    :witan/params {:src ""}}
   {:witan/name :property-comparison
    :witan/version "1.0.0"
    :witan/type :input
    :witan/fn :fire-risk/property-comparison
    :witan/params {:src ""}}
   ;; Calculations functions
   {:witan/name :extract-fire-station-geo-data
    :witan/version "1.0.0"
    :witan/type :function
    :witan/fn :fire-risk/extract-fire-station-geo-data
    :witan/params {:fire-station "Twickenham"}}
   {:witan/name :list-commercial-properties
    :witan/version "1.0.0"
    :witan/type :function
    :witan/fn :fire-risk/list-commercial-properties}
   {:witan/name :group-commercial-properties-type
    :witan/version "1.0.0"
    :witan/type :function
    :witan/fn :fire-risk/group-commercial-properties-type}
   {:witan/name :generic-commercial-properties-fire-risk
    :witan/version "1.0.0"
    :witan/type :function
    :witan/fn :fire-risk/generic-commercial-properties-fire-risk}
   {:witan/name :associate-risk-score-to-commercial-properties
    :witan/version "1.0.0"
    :witan/type :function
    :witan/fn :fire-risk/associate-risk-score-to-commercial-properties}
   {:witan/name :join-historical-and-new-scores
    :witan/version "1.0.0"
    :witan/type :function
    :witan/fn :fire-risk/join-historical-and-new-scores}
   {:witan/name :update-score
    :witan/version "1.0.0"
    :witan/type :function
    :witan/fn :fire-risk/update-score}
   ;; Output function
   {:witan/name :output-new-fire-risk-scores
    :witan/version "1.0.0"
    :witan/type :output
    :witan/fn :fire-risk/output-new-fire-risk-scores}])

(defmodel gwyn-model
  "Defines the model"
  {:witan/name :fire-risk/gwyn-model
   :witan/version "1.0.0"}
  {:workflow gwyn-model-workflow
   :catalog gwyn-model-catalog})

(defn model-library
  "Lists all available functions to execute each step
   in the model"
  []
  (reify p/IModelLibrary
    (available-fns [_]
      (map-fn-meta g/fire-station-lookup-table-1-0-0
                   g/lfb-historic-incidents-1-0-0
                   g/property-comparison-1-0-0
                   g/historical-fire-risk-scores-1-0-0
                   g/extract-fire-station-geo-data-1-0-0
                   g/list-commercial-properties-1-0-0
                   g/group-commercial-properties-type-1-0-0
                   g/generic-commercial-properties-fire-risk-1-0-0
                   g/associate-risk-score-to-commercial-properties-1-0-0
                   g/join-historical-and-new-scores-1-0-0
                   g/update-score-1-0-0
                   g/output-new-fire-risk-scores-1-0-0))
    (available-models [_]
      (map-model-meta gwyn-model))))
