(ns copa.test.db.recipes
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
    (db/create-user! {:username "newuser"
                      :password "pass"
                      :admin    false})
    (f)))

;(deftest get-user
;  (jdbc/with-db-transaction [t-conn *db*]
;                            (jdbc/db-set-rollback-only! t-conn)
;                            (testing "get initial admin user"
;                              (is (= {:username "admin"
;                                      :password "admin"
;                                      :admin    true
;                                      :lang     "en"}
;                                     (dissoc (db/get-user {:username "admin"}) :user_id))))))
;
;(deftest get-all-users
;  (jdbc/with-db-transaction [t-conn *db*]
;                            (jdbc/db-set-rollback-only! t-conn)
;                            (testing "get all users"
;                              (is (= 1 (count (db/get-users t-conn)))))))

(def sample-recipe {:name        "a recipe"
                    :description "a description"
                    :portions    "3"
                    :source      "a source"
                    :preparation "the preparation"
                    :user        "newuser"})

(deftest insert-recipe
  (jdbc/with-db-transaction [t-conn *db*]
                            (jdbc/db-set-rollback-only! t-conn)
                            (testing "inserting new recipe"
                              (is (= 1 (db/create-recipe! t-conn sample-recipe)))
                              (is (= sample-recipe
                                     (dissoc (db/get-recipe-by-name t-conn {:name "a recipe"}) :recipe_id))))
                            (testing "insert another recipe with same name fails"
                              (is (thrown? SQLException
                                           (db/create-recipe! t-conn sample-recipe))))
                            (testing "insert recipe without user fails"
                              (is (thrown? SQLException
                                           (dissoc (assoc (db/create-recipe! t-conn sample-recipe) :name "another name") :user))))
                            (testing "insert recipe without name fails"
                              (is (thrown? SQLException
                                           (dissoc (db/create-recipe! t-conn sample-recipe) :name))))
                            (testing "insert recipe without preparation fails"
                              (is (thrown? SQLException
                                           (dissoc (assoc (db/create-recipe! t-conn sample-recipe) :name "yet another name") :preparation))))))

;(deftest delete-user
;  (jdbc/with-db-transaction [t-conn *db*]
;                            (jdbc/db-set-rollback-only! t-conn)
;                            (testing "deleting a user"
;                              (is (= 1 (db/create-user! t-conn
;                                                        {:username "newuser"
;                                                         :password "pass"
;                                                         :admin    false})))
;                              (is (= 1 (db/delete-user! t-conn
;                                                        {:username "newuser"})))
;                              (is (= nil
;                                     (dissoc (db/get-user t-conn {:username "newuser"}) :user_id))))))
;
;(deftest update-user-password
;  (jdbc/with-db-transaction [t-conn *db*]
;                            (jdbc/db-set-rollback-only! t-conn)
;                            (db/create-user! t-conn
;                                             {:username "newuser"
;                                              :password "pass"
;                                              :admin    false})
;                            (db/update-user-password! t-conn
;                                                      {:username "newuser"
;                                                       :password "p"})
;                            (is (= "p"
;                                   (:password (db/get-user t-conn {:username "newuser"}))))))
;
;(deftest update-user-lang
;  (jdbc/with-db-transaction [t-conn *db*]
;                            (jdbc/db-set-rollback-only! t-conn)
;                            (db/create-user! t-conn
;                                             {:username "newuser"
;                                              :password "pass"
;                                              :admin    false})
;                            (db/update-user-lang! t-conn
;                                                  {:username "newuser"
;                                                   :lang     "cz"})
;                            (is (= "cz"
;                                   (:lang (db/get-user t-conn {:username "newuser"}))))))
