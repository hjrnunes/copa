(ns copa.http.service
  (:require [copa.db.core :refer [*db*] :as db]
            [copa.auth :refer [jws-token]]
            [buddy.hashers :as hashers]
            [ring.util.http-response :refer :all]
            [clj-time.core :as time]
            [cheshire.core :as json]
            [clojure.java.jdbc :as jdbc]
            [copa.util :refer [filter-nil-values]]))

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

(defn- ensure-ingredient [conn name]
  (if-not (db/get-ingredient conn {:name name})
    (db/create-ingredient! conn {:name name})))

(defn- insert-measurement [conn recipe-id measurement]
  (ensure-ingredient conn (:ingredient measurement))
  (let [res (db/create-measurement! conn measurement)
        mid (get res (keyword "scope_identity()"))]
    (db/create-recipe-measurement! conn {:recipe_id      recipe-id
                                         :measurement_id mid})))

(defn- insert-measurements [conn recipe-id measurements]
  (dorun (map #(insert-measurement conn recipe-id %) measurements)))

(defn- get-measurement-ids-for-recipe [conn recipe-id]
  (let [rec-mes (db/get-recipe-measurements-for-recipe conn {:recipe_id recipe-id})]
    (map :measurement_id rec-mes)))

(defn- get-measurements-for-recipe [conn recipe-id]
  (let [mids (get-measurement-ids-for-recipe conn recipe-id)]
    (map filter-nil-values (db/get-measurements-for-ids conn {:measurement_ids mids}))))

(defn- drop-measurements [conn recipe-id]
  (dorun (map #(db/delete-measurement! conn {:measurement_id %}) (get-measurement-ids-for-recipe conn recipe-id))))

(defn- update-measurements [conn recipe-id measurements]
  (drop-measurements conn recipe-id)
  (insert-measurements conn recipe-id measurements))

(defn- insert-recipe [conn recipe]
  (let [res (db/create-recipe! conn recipe)]
    (get res (keyword "scope_identity()"))))

(defn- update-recipe [conn recipe]
  (db/update-recipe! conn recipe))

(defn- get-full-recipe
  ([conn recipe]
   (let [measurements (get-measurements-for-recipe conn (:recipe_id recipe))]
     (filter-nil-values (assoc recipe :measurements measurements))))
  ([recipe]
   (let [measurements (get-measurements-for-recipe *db* (:recipe_id recipe))]
     (filter-nil-values (assoc recipe :measurements measurements)))))

(defn create-recipe [recipe]
  (let [name (:name recipe)
        measurements (:measurements recipe)
        base-recipe (dissoc recipe :measurements)]
    (println recipe)
    (jdbc/with-db-transaction [t-conn *db*]
                              (if (:recipe_id recipe)
                                (do
                                  (update-recipe t-conn base-recipe)
                                  (update-measurements t-conn (:recipe_id recipe) measurements))
                                (let [rid (insert-recipe t-conn base-recipe)]
                                  (update-measurements t-conn rid measurements))))
    (ok (get-full-recipe (db/get-recipe-by-name {:name name})))))

(defn get-all-recipes []
  (ok (dorun (map get-full-recipe (db/get-recipes)))))