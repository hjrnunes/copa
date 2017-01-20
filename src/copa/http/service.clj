(ns copa.http.service
  (:require [copa.db.core :refer [*db*] :as db]
            [copa.auth :refer [jws-token]]
            [buddy.hashers :as hashers]
            [ring.util.http-response :refer :all]
            [clj-time.core :as time]
            [cheshire.core :as json]
            [clojure.java.jdbc :as jdbc]
            [conman.core :refer [with-transaction]]
            [copa.util :refer [filter-nil-values process-exception]])
  (:import (java.sql SQLException)))

(def token-exp-secs 3600)

(defn login [username password]
  (let [user (db/get-user {:username username})]
    (if (hashers/check password (:password user))
      (let [claims {:user  (keyword username)
                    :admin (get user :admin false)
                    :exp   (time/plus (time/now) (time/seconds token-exp-secs))}
            token (jws-token claims)]
        (ok {:token token
             :user  (dissoc user :password :user_id)}))
      (bad-request {:message "Login failed. Wrong auth details."}))))

(defn get-users []
  (ok (map #(dissoc % :password :user_id) (db/get-users))))

(defn- get-user [username]
  (dissoc (db/get-user {:username username}) :password :user_id))

(defn create-user [{:keys [username password admin] :as user}]
  (let [crypted-passwd (hashers/encrypt password)
        res (db/create-user! {:username username :password crypted-passwd :admin admin})]
    (ok (get-user username))))

(defn delete-user [username]
  (db/delete-user! {:username username})
  (ok {:username username}))

(defn update-user-lang [username lang]
  (let [res (db/update-user-lang! {:username username :lang lang})]
    (ok (get-user username))))

(defn update-user-password [username current new confirm]
  (let [user (db/get-user {:username username})]
    (if (hashers/check current (:password user))
      (if (= new confirm)
        (let [crypted-passwd (hashers/encrypt new)]
          (db/update-user-password! {:username username :password crypted-passwd})
          (ok (get-user username)))
        (bad-request {:message (str "New password confirmation does not match for user " username "!")}))
      (bad-request {:message (str "Wrong password for user " username "!")}))))

;; ---- recipes --------------------------------------------

(defn- ensure-ingredient [name]
  (if-not (db/get-ingredient {:name name})
    (db/create-ingredient! {:name name})))

(defn- insert-measurement [recipe-id measurement]
  (ensure-ingredient (:ingredient measurement))
  (let [res (db/create-measurement! (dissoc measurement :measurement_id))
        mid (get res (keyword "scope_identity()"))]
    (db/create-recipe-measurement! {:recipe_id      recipe-id
                                    :measurement_id mid})))

(defn- insert-measurements [recipe-id measurements]
  (dorun (map #(insert-measurement recipe-id %) measurements)))

(defn- get-measurement-ids-for-recipe [recipe-id]
  (let [rec-mes (db/get-recipe-measurements-for-recipe {:recipe_id recipe-id})]
    (map :measurement_id rec-mes)))

(defn- get-measurements-for-recipe [recipe-id]
  (let [mids (get-measurement-ids-for-recipe recipe-id)]
    (map filter-nil-values (db/get-measurements-for-ids {:measurement_ids mids}))))

(defn- drop-measurements [recipe-id]
  (dorun (map #(db/delete-measurement! {:measurement_id %}) (get-measurement-ids-for-recipe recipe-id))))

(defn- update-measurements [recipe-id measurements]
  (drop-measurements recipe-id)
  (insert-measurements recipe-id measurements))

(defn- insert-recipe [recipe]
  (let [res (db/create-recipe! recipe)]
    (get res (keyword "scope_identity()"))))

(defn- update-recipe [recipe]
  (db/update-recipe! recipe))

(defn- get-full-recipe [recipe]
  (let [measurements (get-measurements-for-recipe (:recipe_id recipe))]
    (filter-nil-values (assoc recipe :measurements measurements))))

(defn- prepare-recipe [recipe]
  (update-in recipe [:recipe_id] str))

(defn create-recipe [recipe]
  (let [name (:name recipe)
        measurements (:measurements recipe)
        base-recipe (dissoc recipe :measurements)]
    (try
      (with-transaction [*db*]
                        (if (:recipe_id recipe)
                          (do
                            (update-recipe base-recipe)
                            (update-measurements (:recipe_id recipe) measurements))
                          (let [rid (insert-recipe base-recipe)]
                            (update-measurements rid measurements))))
      (catch SQLException e
        (process-exception e)))
    (ok (prepare-recipe (get-full-recipe (db/get-recipe-by-name {:name name}))))))

(defn get-all-recipes []
  (ok (into [] (comp (map get-full-recipe)
                     (map prepare-recipe)) (db/get-recipes))))

(defn get-recipe-by-name [name]
  (ok (prepare-recipe (get-full-recipe (db/get-recipe-by-name {:name name})))))

(defn delete-recipe-by-id [recipe_id]
  (with-transaction [*db*]
                    (drop-measurements recipe_id)
                    (db/delete-recipe! {:recipe_id recipe_id}))
  (ok recipe_id))

(defn get-recipes-for-ingredient-names [ingredients]
  (doall (map prepare-recipe (db/get-recipes-for-ingredient-name {:ingredients ingredients}))))

;; ----- ingredients ---------------------------

(defn- prepare-ingredient [ingredient]
  (let [recipes (get-recipes-for-ingredient-names [(:name ingredient)])]
    (-> ingredient
        (update-in [:ingredient_id] str)
        (assoc :recipes (map :recipe_id recipes)))))

(defn get-all-ingredients []
  (ok (doall (map prepare-ingredient (db/get-ingredients)))))

(defn get-ingredient-by-name [name]
  (ok (prepare-ingredient (db/get-ingredient {:name name}))))
