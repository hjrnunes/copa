(ns copa.test.db.ingredients
  (:require [copa.db.core :refer [*db*] :as db]
            [luminus-migrations.core :as migrations]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [copa.config :refer [env]]
            [mount.core :as mount])
  (:import (java.sql SQLException)))

(use-fixtures
  :once
  (fn [f]
    (mount/start
      #'copa.config/env
      #'copa.db.core/*db*)
    (migrations/migrate ["reset"] {:database-url "jdbc:h2:./copa_test"})
    (db/create-ingredient! {:name "banana"})
    (f)))

(deftest get-all-ingredients
  (testing "get all users"
    (is (= 1 (count (db/get-ingredients))))))

(deftest insert-ingredient
  (jdbc/with-db-transaction [t-conn *db*]
                            (jdbc/db-set-rollback-only! t-conn)
                            (testing "inserting new ingredient"
                              (is (= 1 (db/create-ingredient! t-conn
                                                              {:name "garlic"})))
                              (is (= {:name "garlic"}
                                     (dissoc (db/get-ingredient t-conn {:name "garlic"}) :ingredient_id))))
                            (testing "insert another ingredient with same name fails"
                              (is (thrown? SQLException
                                           (db/create-ingredient!
                                             t-conn
                                             {:name "garlic"}))))))

(deftest delete-ingredient
  (jdbc/with-db-transaction [t-conn *db*]
                            (jdbc/db-set-rollback-only! t-conn)
                            (testing "deleting an ingredient"
                              (is (= 1 (db/create-ingredient! t-conn
                                                              {:name "garlic"})))
                              (is (= 1 (db/delete-ingredient! t-conn
                                                              {:name "garlic"})))
                              (is (= nil
                                     (dissoc (db/get-ingredient t-conn {:name "garlic"}) :ingredient_id))))))

