(ns kixi.gwyn.schemas
  (:require [schema.core :as s]))

(defn make-ordered-ds-schema [col-vec]
  {:column-names (mapv #(s/one (s/eq (first %)) (str (first %))) col-vec)
   :columns (mapv #(s/one [(second %)] (format "col %s" (name (first %)))) col-vec)
   s/Keyword s/Any})

(defn make-row-schema
  [col-schema]
  (mapv (fn [s] (let [datatype (-> s :schema first)
                      fieldname (:name s)]
                  (s/one datatype fieldname)))
        (:columns col-schema)))

(defn make-col-names-schema
  [col-schema]
  (mapv (fn [s] (let [datatype (:schema s)
                      fieldname (:name s)]
                  (s/one datatype fieldname)))
        (:column-names col-schema)))

(def FireStations
  (make-ordered-ds-schema [[:station s/Str] [:area_km2 java.lang.Double]
                           [:radius java.lang.Double] [:lat java.lang.Double]
                           [:long java.lang.Double]]))

(def LfbHistoricIncidents)

(def HistoricalFireRiskScores)

(def FireStationGeoData)

(def CommercialProperties)

(def GenericFireRisk)

(def CommercialPropertiesWithScores)

(def HistoricalFireRiskScores)

(def NewFireRiskScores)
