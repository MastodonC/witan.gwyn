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
