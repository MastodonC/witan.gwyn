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
  (make-ordered-ds-schema [[:station s/Str] [:area_m2 java.lang.Double]
                           [:radius java.lang.Double] [:lat java.lang.Double]
                           [:long java.lang.Double]]))

(def LfbHistoricIncidents
  (make-ordered-ds-schema [[:property-type s/Str]
                           [:num-pumps-attending s/Int]]))

(def HistoricalFireRiskScores
  {})

(def FireStationGeoData
  (make-ordered-ds-schema [[:radius java.lang.Double] [:lat java.lang.Double]
                           [:long java.lang.Double]]))

(def CommercialProperties
  (make-ordered-ds-schema [[:address s/Str] [:name s/Str] [:type [s/Str]] [:id s/Str]]))

(def CommercialPropertyTypes
  (make-ordered-ds-schema [[:property-type s/Str]
                           [:num-fires s/Int]
                           [:avg-pumps-attending java.lang.Double]
                           [:sd-pumps-attending java.lang.Double]]))

(def GenericFireRisk
  (make-ordered-ds-schema [[:property-type s/Str]
                           [:generic-fire-risk-score s/Int]]))

(def CommercialPropertiesWithScores
  {})

(def HistoricalFireRiskScores
  {})

(def NewFireRiskScores
  {})

(def PropertyComparison
  (make-ordered-ds-schema [[:lfb-property-type s/Str] [:google-property-type s/Str]]))
