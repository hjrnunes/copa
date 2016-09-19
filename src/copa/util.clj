(ns copa.util
  (:require [clojure.string :as str]))

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

(defn filter-nil-values [m]
  (into {} (remove (comp nil? second) m)))

(defn process-exception [e]
  (cond
    (str/includes? (.getMessage e) "PUBLIC.RECIPES(NAME)") (throw (ex-info "Recipe name already exists" {}))
    :else (throw e)))