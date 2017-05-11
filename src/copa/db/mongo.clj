(ns copa.db.mongo
  (:require [config.core :refer [env]]
            [mount.core :refer [defstate]]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.conversion :refer [to-object-id]]
            [monger.query :as q]
            [monger.operators :refer :all]
            [monger.util :refer [object-id]]
            [monger.json]
            [monger.joda-time]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(def uri (if (env :copa-docker)
           (str "mongodb://" (env :copa-db-host) "/copa")
           "mongodb://localhost/copa"))

(defn init-db [uri]
  (info "Connecting to " uri)
  (mg/connect-via-uri uri))

;(defstate mongo
;          :start (init-db uri))

(def users-col "users")
(def recipes-col "recipes")
(def ingredients-col "ingredients")
(def settings-col "settings")

;; -- queries ----------------------------------------

;; settings

(defn get-settings [mongo]
  (mc/find-one-as-map (:db mongo) settings-col {:name "settings"}))

;; prefs

(defn update-lang [mongo username lang]
  (mc/update (:db mongo) users-col {:username username} {$set {:lang lang}}))

(defn update-user-password [mongo username new-password]
  (mc/update (:db mongo) users-col {:username username} {$set {:password new-password}}))

;; users

(defn get-users [mongo]
  (mc/find-maps (:db mongo) users-col))

(defn get-user [mongo username]
  (mc/find-one-as-map (:db mongo) users-col {:username username}))

(defn create-user [mongo username password admin]
  (mc/insert-and-return (:db mongo) users-col {:username username
                                               :password password
                                               :admin    ((partial = "true") admin)}))

(defn delete-user [mongo username]
  (mc/remove (:db mongo) users-col {:username username})
  {:username username})

;; recipes

(defn get-all-recipes [mongo]
  (mc/find-maps (:db mongo) recipes-col))

(defn get-recipes [mongo user]
  (mc/find-maps (:db mongo) recipes-col {:user user}))

(defn get-recipe [mongo name]
  (mc/find-one-as-map (:db mongo) recipes-col {:name name}))

(defn get-recipe-by-id [mongo id]
  (mc/find-by-id (:db mongo) recipes-col (to-object-id id)))

(defn update-recipe [mongo recipe]
  (if-let [sid (:_id recipe)]
    (mc/update-by-id (:db mongo) recipes-col (to-object-id sid) (assoc recipe :_id (to-object-id sid)))
    (mc/update (:db mongo) recipes-col {:name (:name recipe)} recipe {:upsert true})))

(defn remove-recipe [mongo id]
  (mc/remove-by-id (:db mongo) recipes-col (to-object-id id))
  {:_id id})

;; ingredients

(defn get-all-ingredients [mongo]
  (mc/find-maps (:db mongo) ingredients-col))

(defn get-ingredient [mongo name]
  (mc/find-one-as-map (:db mongo) ingredients-col {:name name}))

(defn update-ingredient-recipes [mongo name recipe-id]
  (mc/update (:db mongo) ingredients-col {:name name} {$set      {:name name}
                                                       $addToSet {:recipes (str recipe-id)}} {:upsert true}))

(defn remove-ingredient-recipes [mongo name recipe-id]
  (mc/update (:db mongo) ingredients-col {:name name} {$pull {:recipes recipe-id}}))

