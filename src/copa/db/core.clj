(ns copa.db.core
  (:require [mount.core :refer [defstate]]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as q]
            [monger.operators :refer :all]
            [monger.util :refer [object-id]]
            [monger.json]
            [monger.joda-time]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(def uri "mongodb://localhost/copa")

(defn init-db [uri]
  (mg/connect-via-uri uri))

(defstate mongo
          :start (init-db uri))

;; -- queries ----------------------------------------

;; settings

(defn get-settings [mongo]
  (mc/find-one-as-map (:db mongo) "settings" {:name "settings"}))

;; recipes

(defn get-all-recipes [mongo]
  (mc/find-maps (:db mongo) "recipes"))

(defn get-recipe [mongo name]
  (mc/find-one-as-map (:db mongo) "recipes" {:name name}))

(defn update-recipe [mongo recipe]
  (mc/update (:db mongo) "recipes" {:name (:name recipe)} recipe {:upsert true}))

;; ingredients

(defn get-all-ingredients [mongo]
  (mc/find-maps (:db mongo) "ingredients"))

(defn get-ingredient [mongo name]
  (mc/find-one-as-map (:db mongo) "ingredients" {:name name}))

(defn update-ingredient [mongo name recipe-name]
  (mc/update (:db mongo) "ingredients" {:name name} {$set      {:name name}
                                                     $addToSet {:recipes recipe-name}} {:upsert true}))

