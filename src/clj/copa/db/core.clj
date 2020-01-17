(ns copa.db.core
  (:require
    [clojure.java.jdbc :as jdbc]
    [conman.core :as conman]
    [java-time.pre-java8 :as jt]
    [mount.core :refer [defstate]]
    [crux.api :as crux]
    [copa.config :refer [env]])
  (:import (crux.api ICruxAPI)))


(def initial-data [[:crux.tx/put
                    #:recipe{:crux.db/id   :penne-alla-senese
                             :id           "penne-alla-senese",
                             :name         "Penne alla senese",
                             :description  "Penne com salsicha, nozes e natas",
                             :preparation  "*notas ingredientes*\n\n- *salsichas*: desfeitas sem pele\n- *nozes*: sem casca, picadas finas",
                             :user         "admin",
                             :measurements [#:measure{:quantity   20.0,
                                                      :unit       "g",
                                                      :ingredient "manteiga"
                                                      },
                                            #:measure{:quantity   1.0,
                                                      :unit       "colheres",
                                                      :ingredient "café"}
                                            #:measure{:quantity   1.0,
                                                      :ingredient "marmelo"}
                                            #:measure{:ingredient "laranja"}]}]
                   [:crux.tx/put
                    #:recipe{:crux.db/id   :penne-alla-senese-2
                             :id           "penne-alla-senese-2",
                             :name         "Penne alla senese 2",
                             :description  "Penne com salsicha, nozes e natas",
                             :preparation  "*notas ingredientes*\n\n- *salsichas*: desfeitas sem pele\n- *nozes*: sem casca, picadas finas",
                             :user         "admin",
                             :measurements [#:measure{:quantity   20.0,
                                                      :unit       "g",
                                                      :ingredient "manteiga"
                                                      },
                                            #:measure{:quantity   1.0,
                                                      :unit       "colheres",
                                                      :ingredient "café"}]}]
                   ])

(defstate ^:dynamic *db*
  :start (let [node ^ICruxAPI (crux/start-node {:crux.node/topology                 :crux.standalone/topology
                                                :crux.node/kv-store                 "crux.kv.lmdb/kv"
                                                :crux.kv/db-dir                     "data/db-dir-1"
                                                :crux.standalone/event-log-dir      "data/eventlog-1"
                                                :crux.standalone/event-log-kv-store "crux.kv.lmdb/kv"})]
           (crux/submit-tx node initial-data)
           node)
  :stop (conman/disconnect! *db*))


(defn bootstrap []
  "Bootstrap client DB. For now return all recipes"
  (let [db (crux/db *db*)]
    (->> (crux/q db '{:find  [?x]
                      :where [[?x :recipe/id _]]})
         (map first)
         (map #(crux/entity db %)))))


;;;;;;;

;(defstate ^:dynamic *db*
;  :start (conman/connect! {:jdbc-url (env :database-url)})
;  :stop (conman/disconnect! *db*))

;(conman/bind-connection *db* "sql/queries.sql")


(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Timestamp
  (result-set-read-column [v _2 _3]
    (.toLocalDateTime v))
  java.sql.Date
  (result-set-read-column [v _2 _3]
    (.toLocalDate v))
  java.sql.Time
  (result-set-read-column [v _2 _3]
    (.toLocalTime v)))

(extend-protocol jdbc/ISQLValue
  java.util.Date
  (sql-value [v]
    (java.sql.Timestamp. (.getTime v)))
  java.time.LocalTime
  (sql-value [v]
    (jt/sql-time v))
  java.time.LocalDate
  (sql-value [v]
    (jt/sql-date v))
  java.time.LocalDateTime
  (sql-value [v]
    (jt/sql-timestamp v))
  java.time.ZonedDateTime
  (sql-value [v]
    (jt/sql-timestamp v)))


