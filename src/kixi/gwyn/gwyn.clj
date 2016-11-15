(ns kixi.gwyn
  (:require [witan.workspace-api :refer [defworkflowfn definput defworkflowoutput]]
            [kixi.gwyn.schemas :as sc]))

(definput fire-station-lookup-table-1-0-0
  {:witan/name :gwyn/fire-station-lookup-table
   :witan/version "1.0.0"
   :witan/key :fire-station-lookup-table
   :witan/schema sc/FireStations})
