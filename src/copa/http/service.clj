(ns copa.http.service
  (:require [copa.db.core :as db :refer [mongo]]
            [ring.util.http-response :refer :all]
            [cheshire.core :as json]))


(defn get-all-recipes []
  (ok (db/get-all-recipes mongo)))

(defn get-all-ingredients []
  (ok (db/get-all-ingredients mongo)))

(defn get-recipe-by-name [name]
  (ok (db/get-recipe mongo name)))

(defn get-ingredient-by-name [name]
  (ok (db/get-ingredient mongo name)))

(defn create-recipe [recipe]
  (let [updt-res (db/update-recipe mongo recipe)]
    (ok (db/get-recipe mongo (:name recipe)))))
