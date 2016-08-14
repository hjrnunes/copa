(ns copa.util
  (:require [copa.lang.pt :as lang-pt]
            [copa.lang.en :as lang-en]))

(defn vec-remove
  "remove elem in coll"
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

(def langs {:pt lang-pt/lang
            :en lang-en/lang})

(defn t [lang s]
  (get-in langs [lang s]))