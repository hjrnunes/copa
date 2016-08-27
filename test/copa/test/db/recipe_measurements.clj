(ns copa.test.db.recipe-measurements
  (:require [copa.db.core :refer [*db*] :as db]
            [luminus-migrations.core :as migrations]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [copa.config :refer [env]]
            [mount.core :as mount])
  (:import (java.sql SQLException)))

(def sample-recipe {:name        "a recipe"
                    :description "a description"
                    :portions    "3"
                    :source      "a source"
                    :preparation "the preparation"
                    :user        "newuser"})

(def sample-measurement {:ingredient "garlic"
                         :quantity   1
                         :unit       "g"})

(use-fixtures
  :once
  (fn [f]
    (mount/start
      #'copa.config/env
      #'copa.db.core/*db*)
    (migrations/migrate ["reset"] {:database-url "jdbc:h2:./copa_test;MVCC=true"})
    (db/create-user! {:username "newuser"
                      :password "pass"
                      :admin    false})
    (db/create-ingredient! {:name "garlic"})
    (f)))

(deftest get-all-ingredients
  (testing "get all users"
    (is (= 1 (count (db/get-ingredients))))))

(defn create-measurement [t-conn]
  (get (db/create-measurement! t-conn sample-measurement)
       (keyword "scope_identity()")))

(deftest insert-recipe-measurement
  (jdbc/with-db-transaction [t-conn *db*]
                            (jdbc/db-set-rollback-only! t-conn)
                            (let [recres (db/create-recipe! t-conn sample-recipe)
                                  rid (get recres (keyword "scope_identity()"))
                                  mid (create-measurement t-conn)]
                              (testing "insert a new recipe measurement"
                                (db/create-recipe-measurement! t-conn {:recipe_id      rid
                                                                       :measurement_id mid})
                                (let [rms (db/get-recipe-measurements-for-recipe t-conn {:recipe_id rid})]
                                  (is (= 1 (count rms)))
                                  (is (= {:recipe_id      rid
                                          :measurement_id mid}
                                         (dissoc (first rms) :rec_measurement_id)))))
                              (testing "insert another recipe measurement"
                                (let [mid2 (create-measurement t-conn)]
                                  (db/create-recipe-measurement! t-conn {:recipe_id      rid
                                                                         :measurement_id mid2})
                                  (let [rms (db/get-recipe-measurements-for-recipe t-conn {:recipe_id rid})]
                                    (is (= 2 (count rms)))
                                    (is (= {:recipe_id      rid
                                            :measurement_id mid2}
                                           (dissoc (second rms) :rec_measurement_id))))))
                              (testing "insert a recipe measurement without recipe_id fails"
                                (is (thrown? SQLException
                                             (db/create-recipe-measurement! t-conn {:measurement_id mid
                                                                                    :recipe_id      nil}))))
                              (testing "insert a recipe measurement without measurement_id fails"
                                (is (thrown? SQLException
                                             (db/create-recipe-measurement! t-conn {:recipe_id      rid
                                                                                    :measurement_id nil})))))))


