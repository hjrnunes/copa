(ns copa.db.query
  (:require [copa.db.core :refer [as-db]]
            [datomic.api :refer [q pull] :as d]))

(defn get-all-recipes [db]
  (let [db (as-db db)]
    (q '[:find (pull ?e [*])
         :where [?e :recipe/name _]]
       db)))