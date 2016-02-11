(ns copa.util)

(defn in? [coll e]
  (some #{e} coll))