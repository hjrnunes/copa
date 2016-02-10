(ns copa.db.core
  (:require [mount.core :refer [defstate]]
            [datomic.api :as d]
            [io.rkn.conformity :as c]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(def uri "datomic:mem://copa")

(defn init-db []
  (info "Init db")
  (d/create-database uri)
  (let [conn (d/connect uri)
        norms-map (c/read-resource "copa-schema.edn")]
    (c/ensure-conforms conn norms-map [:copa/schema
                                       :copa/seed])
    conn))

(defstate conn
          :start (init-db))


