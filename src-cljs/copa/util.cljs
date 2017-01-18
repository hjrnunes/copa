(ns copa.util
  (:require [cuerdas.core :as str]
            [copa.lang.pt :as lang-pt]
            [copa.lang.en :as lang-en]))

(defn vec-remove
  "remove elem in coll"
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

(def langs {:pt lang-pt/lang
            :en lang-en/lang})

(defn t [lang s]
  (get-in langs [lang s]))

(defn capitalize [s]
  (let [initial (str/slice s 0 1)
        res (str/slice s 1)]
    (str (str/capital initial) res)))