(ns copa.http.service
  (:require [copa.db.core :refer [*db*] :as db]
            [copa.auth :refer [jws-token]]
            [buddy.hashers :as hashers]
            [ring.util.http-response :refer :all]
            [clj-time.core :as time]
            [cheshire.core :as json]))

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

;; UPDATE
;; drop all measurements
;; insert
;; create/update recipe details

(defn- ensure-ingredient [name]
  (if-not (db/get-ingredient! {:name name})
    (db/create-ingredient! {:name name})))

(defn- insert-measurement [recipe-id measurement]
  (ensure-ingredient (:ingredient measurement))
  (let [res (db/create-measurement! measurement)
        mid (get res (keyword "scope_identity()"))]
    (db/create-recipe-measurement! {:recipe_id      recipe-id
                                    :measurement_id mid})))

(defn- insert-measurements [recipe-id measurements]
  (dorun (map #(insert-measurement recipe-id %) measurements)))

(defn- drop-measurements [recipe-id]
  (let [rec-mes (db/get-recipe-measurements-for-recipe {:recipe_id recipe-id})
        measurement-ids (map :measurement_id rec-mes)]
    (dorun (map #(db/delete-measurement! {:measurement_id %}) measurement-ids))))

(defn- update-measurements [recipe-id measurements]
  (drop-measurements recipe-id)
  (insert-measurements recipe-id measurements))

(defn- insert-recipe [recipe]
  (let [res (db/create-recipe! recipe)]
    (get res (keyword "scope_identity()"))))

(defn- update-recipe [recipe]
  (db/update-recipe! recipe))

(defn create-recipe [recipe]
  (let [name (:name recipe)
        measurements (:measurements recipe)
        base-recipe (dissoc recipe :measurements)]
    (jdbc/with-db-transaction (if (:recipe_id recipe)
                                (do
                                  (update-recipe base-recipe)
                                  (update-measurements (:recipe_id recipe) measurements))
                                (let [rid (insert-recipe base-recipe)]
                                  (update-measurements rid measurements))))
    (ok (db/get-recipe-by-name {:name name}))))