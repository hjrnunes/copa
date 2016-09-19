(ns copa.http.old_service
  (:require
            ;[copa.db.mongo :as db :refer [mongo]]
            [copa.auth :refer [jws-token]]
            [buddy.hashers :as hashers]
            [ring.util.http-response :refer :all]
            [clj-time.core :as time]
            [cheshire.core :as json]))

;; -- auth -----------------------------------------------------

(def token-exp-secs 3600)

;(defn login [username password]
;  (let [user (db/get-user mongo username)]
;    (if (hashers/check password (:password user))
;      (let [claims {:user  (keyword username)
;                    :admin (get user :admin false)
;                    :exp   (time/plus (time/now) (time/seconds token-exp-secs))}
;            token (jws-token claims)]
;        (ok {:token token
;             :user  (dissoc user :password)}))
;      (bad-request {:message "Login failed. Wrong auth details."}))))
;
;(defn get-users []
;  (ok (map #(dissoc % :password) (db/get-users mongo))))
;
;(defn create-user [{:keys [username password admin] :as user}]
;  (let [crypted-passwd (hashers/encrypt password)]
;    (ok (dissoc (db/create-user mongo username crypted-passwd admin) :password))))
;
;(defn delete-user [username]
;  (ok (db/delete-user mongo username)))
;
;(defn get-settings []
;  (ok (db/get-settings mongo)))
;
;(defn get-all-recipes []
;  (ok (db/get-all-recipes mongo)))
;
;(defn get-recipes [user]
;  (ok (db/get-recipes mongo user)))
;
;(defn get-all-ingredients []
;  (ok (db/get-all-ingredients mongo)))
;
;(defn get-recipe-by-name [name]
;  (ok (db/get-recipe mongo name)))
;
;(defn delete-recipe-by-id [id]
;  (let [recipe (db/get-recipe-by-id mongo id)
;        ingredients (map :ingredient (:measurements recipe))]
;    (doall (map #(db/remove-ingredient-recipes mongo % id) ingredients))
;    (ok (db/remove-recipe mongo id))))
;
;(defn get-ingredient-by-name [name]
;  (ok (db/get-ingredient mongo name)))
;
;(defn create-recipe [recipe]
;  (let [ingredients (map :ingredient (:measurements recipe))
;        updt-res (db/update-recipe mongo recipe)
;        rid (or (:_id recipe) (.getUpsertedId updt-res))
;        ingr-res (doall (map #(db/update-ingredient-recipes mongo % rid) ingredients))]
;    (ok (db/get-recipe-by-id mongo rid))))
;
;(defn update-user-lang [username lang]
;  (let [res (db/update-lang mongo username lang)
;        user (db/get-user mongo username)]
;    (if (= 1 (.getN res))
;      (ok (assoc user :lang lang))
;      (ok user))))
;
;(defn update-user-password [username current new confirm]
;  (let [user (db/get-user mongo username)]
;    (if (hashers/check current (:password user))
;      (if (= new confirm)
;        (let [crypted-passwd (hashers/encrypt new)]
;          (db/update-user-password mongo username crypted-passwd)
;          (ok (dissoc (db/get-user mongo username) :password)))
;        (bad-request {:message (str "New password confirmation does not match for user " username "!")}))
;      (bad-request {:message (str "Wrong password for user " username "!")}))))