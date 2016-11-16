(ns kixi.gwyn.model
  (:require [witan.workspace-api :refer [defmodel]]
            [witan.workspace-api.protocols :as p]
            [witan.workspace-api.utils :refer [map-fn-meta
                                               map-model-meta]]))

(def gwyn-model-workflow
  "Define each step of the model"
  [])

(def gwyn-model-catalog
  "Provides metadata for each step in the model"
  [])

(defmodel gwyn-model
  "Defines the model"
  {:witan/name :gwyn-model
   :witan/version "1.0.0"}
  {:workflow gwyn-model-workflow
   :catalog gwyn-model-catalog})

(defn model-library
  "Lists all available functions to execute each step
   in the model"
  []
  (reify p/IModelLibrary
    (available-fns [_]
      (map-fn-meta))
    (available-models [_]
      (map-model-meta gwyn-model))))
