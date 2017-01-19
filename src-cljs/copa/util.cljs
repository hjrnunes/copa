(ns copa.util
  (:require [cuerdas.core :as str]
            [copa.lang.pt :as lang-pt]
            [copa.lang.en :as lang-en]
            [copa.db]
            [re-frame.core :refer [trim-v after]]
            [cljs.spec :as s]))

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

(defn check-and-throw
  "throw an exception if db doesn't match the spec"
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

(def check-spec-interceptor (after (partial check-and-throw :copa.db/db)))

(def common-interceptors [check-spec-interceptor
                          trim-v])