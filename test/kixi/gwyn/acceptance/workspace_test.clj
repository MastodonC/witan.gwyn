(ns kixi.gwyn.acceptance.workspace-test
  (:require [clojure.test :refer :all]
            [kixi.gwyn.gwyn :refer :all]
            [schema.core :as s]
            [kixi.gwyn.schemas :as sc]
            [kixi.gwyn.model :as m]
            [witan.workspace-api.protocols :as p]
            [witan.workspace-executor.core :as wex]))
