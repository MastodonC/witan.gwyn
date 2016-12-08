(ns witan.gwyn.utils)

(defn make-coll [x]
  (cond
    (seq? x) x
    (vector? x) x
    :else (vector x)))

(defn safe-divide
  [d dd]
  (if (zero? dd)
    0.0
    (/ ^double d dd)))

(def unwanted-property-types
  ["administrative_area_level_1"
   "administrative_area_level_2"
   "administrative_area_level_3"
   "administrative_area_level_4"
   "administrative_area_level_5"
   "colloquial_area"
   "country"
   "establishment"
   "finance"
   "floor"
   "food"
   "general_contractor"
   "geocode"
   "health"
   "intersection"
   "locality"
   "natural_feature"
   "neighborhood"
   "place_of_worship"
   "political"
   "point_of_interest"
   "post_box"
   "postal_code"
   "postal_code_prefix"
   "postal_code_suffix"
   "postal_town"
   "premise"
   "room"
   "route"
   "street_address"
   "street_number"
   "sublocality"
   "sublocality_level_4"
   "sublocality_level_5"
   "sublocality_level_3"
   "sublocality_level_2"
   "sublocality_level_1"
   "subpremise"])
