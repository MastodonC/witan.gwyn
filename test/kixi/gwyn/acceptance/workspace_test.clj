(ns kixi.gwyn.acceptance.workspace-test
  (:require [clojure.test :refer :all]
            [kixi.gwyn.gwyn :refer :all]
            [schema.core :as s]
            [kixi.gwyn.schemas :as sc]
            [kixi.gwyn.model :as m]
            [witan.workspace-api.protocols :as p]
            [witan.workspace-executor.core :as wex]))

(def test-inputs
  {:fire-stations-lookup {}
   :lfb-historic-incidents {}
   :historical-fire-risk-scores {}})

(defn read-inputs [input _ schema]
  (get test-inputs (:witan/name input)))

(defn add-input-params
  [input]
  (assoc-in input [:witan/params :fn] (partial read-inputs input)))

(deftest gwyn-workspace-test
  (testing "The model is run on the workspace and returns the outputs expected"
    (let [fixed-catalog (mapv #(if (= (:witan/type %) :input) (add-input-params %) %)
                              (:catalog m/gwyn-model))
          workspace     {:workflow  (:workflow m/gwyn-model)
                         :catalog   fixed-catalog
                         :contracts (p/available-fns (m/model-library))}
          workspace'    (s/with-fn-validation (wex/build! workspace))
          result        (apply merge (wex/run!! workspace' {}))]
      (is result)
      ;;(is (:new-fire-risk-scores result))
      )))
