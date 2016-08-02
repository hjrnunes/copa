(ns copa.http.service
  (:require [copa.db.core :as db :refer [mongo]]
            [copa.auth :refer [jws-token]]
            [buddy.hashers :as hashers]
            [ring.util.http-response :refer :all]
            [clj-time.core :as time]
            [cheshire.core :as json]))

;; -- auth -----------------------------------------------------

(defn login [username password]
  (let [user (db/get-user mongo username)]
    (if (hashers/check password (:password user))
      (let [claims {:user  (keyword username)
                    :admin (get user :admin false)
                    :exp   (time/plus (time/now) (time/seconds 3600))}
            token (jws-token claims)]
        (ok {:token token
             :user  (dissoc user :password)}))
      (bad-request {:message "Login failed. Wrong auth details."}))))

(defn get-users []
  (ok (map #(dissoc % :password) (db/get-users mongo))))

(defn create-user [{:keys [username password admin] :as user}]
  (let [crypted-passwd (hashers/encrypt password)]
    (ok (dissoc (db/create-user mongo username crypted-passwd admin) :password))))

(defn delete-user [username]
  (ok (db/delete-user mongo username)))

(defn get-settings []
  (ok (db/get-settings mongo)))

(defn get-all-recipes []
  (ok (db/get-all-recipes mongo)))

(defn get-recipes [user]
  (ok (db/get-recipes mongo user)))

(defn get-all-ingredients []
  (ok (db/get-all-ingredients mongo)))

(defn get-recipe-by-name [name]
  (ok (db/get-recipe mongo name)))

(defn delete-recipe-by-name [name]
  (ok (db/remove-recipe mongo name)))

(defn get-ingredient-by-name [name]
  (ok (db/get-ingredient mongo name)))

(defn create-recipe [recipe]
  (let [ingredients (map :ingredient (:measurements recipe))
        updt-res (db/update-recipe mongo recipe)
        ingr-res (doall (map #(db/update-ingredient mongo % (:name recipe)) ingredients))]
    (ok (db/get-recipe mongo (:name recipe)))))
