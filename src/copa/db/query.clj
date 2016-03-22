(ns copa.db.query
  (:require [juxt.datomic.extras :refer [to-ref-id as-db db? to-entity-map]]
            [datomic.api :refer [q pull transact] :as d]
            [copa.db.core :refer [conn?]]
            [copa.util :refer [in? ?assoc]]
            [clojure.pprint :refer [pprint]]))

(defn transact-insert
  "Blocking update. Returns the entity map of the new entity."
  [conn temps txdata]
  {:pre [(conn? conn)
         (or (number? temps) (coll? temps))
         (coll? txdata)]}
  (pprint txdata)
  (let [{:keys [db-after tempids]} @(transact conn (vec txdata))]
    (if (vector? temps)
      (map (comp (partial d/entity db-after)
                 (partial d/resolve-tempid db-after tempids)) temps)
      (d/entity db-after (d/resolve-tempid db-after tempids temps)))))

(defn pull-all [db atrib]
  (let [db (as-db db)]
    (map first
         (q '[:find (pull ?e [*])
              :in $ ?atrib
              :where [?e ?atrib _]]
            db atrib))))

(defn pull-all-ingredients [db]
  (pull-all db :ingredient/name))

(defn pull-all-recipes [db]
  (let [db (as-db db)]
    (map first
         (q '[:find (pull ?e [* {:recipe/measurements [* {:measurement/ingredient [*]}]}])
              :where [?e :recipe/name _]]
            db))))

(defn pull-by-atribute [db atrib value]
  (let [db (as-db db)]
    (ffirst
      (q '[:find (pull ?e [*])
           :in $ ?atrib ?value
           :where [?e ?atrib ?value]]
         db atrib value))))

(defn pull-ingredient-by-name [db name]
  (pull-by-atribute db :ingredient/name name))

(defn pull-recipe [db eid]
  (let [db (as-db db)]
    (ffirst
      (q '[:find (pull ?eid [* {:recipe/measurements [* {:measurement/ingredient [*]}]}])
           :in $ ?eid
           :where [?eid]]
         db eid))))

(defn pull-recipe-by-name [db name]
  (let [db (as-db db)]
    (ffirst
      (q '[:find (pull ?e [* {:recipe/measurements [* {:measurement/ingredient [*]}]}])
           :in $ ?value
           :where [?e :recipe/name ?value]]
         db name))))

(defn ingredient-names [ingredients]
  ;; new ingredients are strings, existing are maps
  (for [ingredient ingredients]
    (if (map? ingredient)
      (:ingredient/name ingredient)
      ingredient)))

(defn find-ingredients [db ingredients]
  (let [db (as-db db)]
    (q '[:find (pull ?ing [*])
         :in $ [?ing-name ...]
         :where [?ing :ingredient/name ?ing-name]]
       db (ingredient-names ingredients))))

(defn process-measurements [db measurements]
  (let [rec-ingredients (map :measurement/ingredient measurements)
        existing (map first (find-ingredients db rec-ingredients))
        existing-names (map :ingredient/name existing)]
    (vec (for [{:keys [measurement/ingredient measurement/quantity measurement/unit] :as measurement} measurements]
           (if (map? ingredient)
             measurement
             ;; if ingredient is not a map, it's new
             (?assoc {}
                     :measurement/quantity quantity
                     :measurement/unit unit
                     :measurement/ingredient (if (in? existing-names ingredient)
                                               ;; if ingredient exists refer to by lookup ref
                                               [:ingredient/name ingredient]
                                               ;; else just nest create
                                               {:ingredient/name ingredient})))))))

(defn create-recipe! [db & [{:keys [recipe/name recipe/description recipe/portions recipe/preparation recipe/categories recipe/measurements]}]]
  (let [recipe (d/tempid :db.part/user)]
    (->> [[:db/id recipe]
          [:recipe/name name]
          (when description [:recipe/description description])
          (when portions [:recipe/portions portions])
          [:recipe/preparation preparation]
          (when categories [:recipe/categories categories])
          [:recipe/measurements (process-measurements db measurements)]]
         (remove nil?) vec
         (into {})
         (vector)
         (transact-insert db recipe))))