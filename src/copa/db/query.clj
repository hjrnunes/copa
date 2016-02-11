(ns copa.db.query
  (:require [juxt.datomic.extras :refer [to-ref-id as-db db? to-entity-map]]
            [datomic.api :refer [q pull transact] :as d]
            [copa.db.core :refer [conn?]]
            [copa.util :refer [in?]]
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

(defn get-all-recipes [db]
  (let [db (as-db db)]
    (q '[:find (pull ?e [*])
         :where [?e :recipe/name _]]
       db)))

(defn get-ingredient-by-name [db name]
  (let [db (as-db db)]
    (q '[:find (pull ?e [*])
         :in $ ?name
         :where [?e :ingredient/name ?name]]
       db name)))

(defn find-ingredients [db ingredient-names]
  (let [db (as-db db)]
    (q '[:find (pull ?ing [*])
         :in $ [?ing-name ...]
         :where [?ing :ingredient/name ?ing-name]]
       db ingredient-names)))

(defn process-measurements [db measurements]
  (let [rec-ingredients (map :measurement/ingredient measurements)
        existing (map first (find-ingredients db rec-ingredients))
        existing-names (map :ingredient/name existing)]
    (vec (for [{:keys [measurement/ingredient measurement/quantity measurement/unit]} measurements]
           (assoc {:measurement/quantity quantity
                   :measurement/unit     unit}
             :measurement/ingredient
             (if (in? existing-names ingredient)
               ;; if ingredient exists refer to by lookup ref
               ;(to-ref-id (:db/id (first (get (group-by :ingredient/name existing) ingredient))))
               [:ingredient/name ingredient]
               ;; else just nest create
               {:ingredient/name ingredient}))))))

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