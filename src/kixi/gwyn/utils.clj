(ns kixi.gwyn.utils)

(defn make-coll [x]
  (cond
    (seq? x) x
    (vector? x) x
    :else (vector x)))
