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
      (let [claims {:user (keyword username)
                    :exp  (time/plus (time/now) (time/seconds 60))}
            token (jws-token claims)]
        (ok {:token token
             :user  (dissoc user :password)}))
      (bad-request {:message "Login failed. Wrong auth details."}))))

(defn get-settings []
  (ok (db/get-settings mongo)))

(defn get-all-recipes []
  (ok (db/get-all-recipes mongo)))

(defn get-all-ingredients []
  (ok (db/get-all-ingredients mongo)))

(defn get-recipe-by-name [name]
  (ok (db/get-recipe mongo name)))

(defn get-ingredient-by-name [name]
  (ok (db/get-ingredient mongo name)))

(defn create-recipe [recipe]
  (let [ingredients (map :ingredient (:measurements recipe))
        updt-res (db/update-recipe mongo recipe)
        ingr-res (doall (map #(db/update-ingredient mongo % (:name recipe)) ingredients))]
    (ok (db/get-recipe mongo (:name recipe)))))
