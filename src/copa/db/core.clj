(ns copa.db.core
  (:require [config.core :refer [env]]
            [mount.core :refer [defstate]]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as q]
            [monger.operators :refer :all]
            [monger.util :refer [object-id]]
            [monger.json]
            [monger.joda-time]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(def uri (if (env :copa-docker)
           (str "mongodb://" (env :db-port-27017-tcp-addr) "/copa")
           "mongodb://localhost/copa"))

(defn init-db [uri]
  (mg/connect-via-uri uri))

(defstate mongo
          :start (init-db uri))

(def users-col "users")
(def recipes-col "recipes")
(def ingredients-col "ingredients")
(def settings-col "settings")

;; -- queries ----------------------------------------

;; settings

(defn get-settings [mongo]
  (mc/find-one-as-map (:db mongo) settings-col {:name "settings"}))

;; users

(defn get-users [mongo]
  (mc/find-maps (:db mongo) users-col))

(defn get-user [mongo username]
  (mc/find-one-as-map (:db mongo) users-col {:username username}))

(defn create-user [mongo username password admin]
  (mc/insert-and-return (:db mongo) users-col {:username username
                                               :password password
                                               :admin    ((partial = "true") admin)}))

(defn update-user-password [mongo username new-password]
  (mc/update (:db mongo) users-col {:username username} {$set {:password new-password}}))

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

(defn update-recipe [mongo recipe]
  (mc/update (:db mongo) recipes-col {:name (:name recipe)} recipe {:upsert true}))

(defn remove-recipe [mongo name]
  (mc/remove (:db mongo) recipes-col {:name name})
  {:name name})

;; ingredients

(defn get-all-ingredients [mongo]
  (mc/find-maps (:db mongo) ingredients-col))

(defn get-ingredient [mongo name]
  (mc/find-one-as-map (:db mongo) ingredients-col {:name name}))

(defn update-ingredient [mongo name recipe-name]
  (mc/update (:db mongo) ingredients-col {:name name} {$set      {:name name}
                                                       $addToSet {:recipes recipe-name}} {:upsert true}))

