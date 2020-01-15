(ns copa.db
  (:require
    [re-posh.core :as rp]
    [datascript.core :as ds]
    ))

(defn new-entity! [conn varmap]
  ((:tempids (ds/transact! conn [(merge varmap {:db/id -1})])) -1))

(def tempid (let [n (atom 0)] (fn [] (swap! n dec))))


(def initial-db [(assoc #:recipe{:id           "penne-alla-senese",
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
                                                #:measure{:ingredient "laranja"}]} :db/id (tempid))
                 (assoc #:recipe{:id           "penne-alla-senese-2",
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
                                                          :ingredient "café"}]} :db/id (tempid))
                 ])


(def conn (ds/create-conn {:recipe/id           {:db/unique :db.unique/identity}

                           :recipe/measurements {:db/valueType   :db.type/ref
                                                 :db/isComponent true
                                                 :db/cardinality :db.cardinality/many}}))

(rp/connect! conn)