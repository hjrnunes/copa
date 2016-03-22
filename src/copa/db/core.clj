(ns copa.db.core
  (:require [mount.core :refer [defstate]]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as q]
            [monger.operators :refer :all]
            [monger.util :refer [object-id]]
            [monger.json]
            [monger.joda-time]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre])
  (:import (com.mongodb MongoClientException)))

(timbre/refer-timbre)

(def uri "mongodb://localhost/copa")

(defn init-db [uri]
  (mg/connect-via-uri uri))

(defstate mongo
          :start (init-db uri))

;; -- queries ----------------------------------------

(defn get-all-recipes [mongo]
  (mc/find-maps (:db mongo) "recipes"))

(defn get-all-ingredients [mongo]
  (mc/find-maps (:db mongo) "ingredients"))

(defn get-recipe [mongo name]
  (mc/find-one-as-map (:db mongo) "recipes" {:name name}))

(defn get-ingredient [mongo name]
  (mc/find-one-as-map (:db mongo) "ingredients" {:name name}))

(defn update-recipe [mongo recipe]
  (mc/update (:db mongo) "recipes" {:name (:name recipe)} recipe {:upsert true}))



