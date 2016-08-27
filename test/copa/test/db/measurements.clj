(ns copa.test.db.measurements
  (:require [copa.db.core :refer [*db*] :as db]
            [luminus-migrations.core :as migrations]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [copa.config :refer [env]]
            [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (mount/start
      #'copa.config/env
      #'copa.db.core/*db*)
    (migrations/migrate ["reset"] {:database-url "jdbc:h2:./copa_test;MVCC=true"})
    (f)))

(deftest insert-measurement
  (jdbc/with-db-transaction [t-conn *db*]
                            (jdbc/db-set-rollback-only! t-conn)
                            (testing "inserting new measurement"
                              (db/create-ingredient! t-conn
                                                     {:name "garlic"})
                              (let [res (db/create-measurement! t-conn
                                                                {:ingredient "garlic"
                                                                 :quantity   1
                                                                 :unit       "g"})
                                    id (get res (keyword "scope_identity()"))]
                                (is (= "garlic"
                                       (:ingredient (db/get-measurement t-conn {:measurement_id id}))))))))

