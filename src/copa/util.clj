(ns copa.util)

(defn in? [coll e]
  (some #{e} coll))

(defn ?assoc
  "Same as assoc, but skip the assoc if v is nil"
  [m & kvs]
  (->> kvs
       (partition 2)
       (filter second)
       flatten
       (apply assoc m)))
