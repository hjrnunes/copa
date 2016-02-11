(ns copa.db.core
  (:require [mount.core :refer [defstate]]
            [datomic.api :as d]
            [io.rkn.conformity :as c]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(def uri "datomic:mem://copa")

;(defn init-db []
;  (info "Init db")
;  (d/create-database uri)
;  (let [conn (d/connect uri)
;        norms-map (c/read-resource "copa-schema.edn")]
;    (c/ensure-conforms conn norms-map [:copa/schema
;                                       :copa/seed])
;    conn))

(defn init-db [dburi]
  {:pre [dburi]}
  (if (d/create-database dburi)
    (debug "Created database" dburi)
    (debug "Using existing database" dburi))
  (let [conn (d/connect dburi)
        norms-map (c/read-resource "copa-schema.edn")]
    (debug "Loading schema")
    (c/ensure-conforms conn norms-map [:copa/schema
                                       :copa/seed])
    conn))


(defstate db
          :start (init-db uri))


(defprotocol DatabaseReference
  (as-db [_]))

(extend-protocol DatabaseReference
  datomic.db.Db
  (as-db [db] db)
  datomic.Connection
  (as-db [conn] (d/db conn))
  String
  (as-db [dburi] (as-db (d/connect dburi))))