(ns copa.test.db.users
  (:require [copa.db.core :refer [*db*] :as db]
            [luminus-migrations.core :as migrations]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [copa.config :refer [env]]
            [mount.core :as mount])
  (:import (java.sql SQLException)))

(def passw "bcrypt+sha512$a20e2dd70567d58028a5a108ce54b832$12$00a281eb5acd4dd11c37dffadc5f4aeb78288684029268a9")

(use-fixtures
  :once
  (fn [f]
    (mount/start
      #'copa.config/env
      #'copa.db.core/*db*)
    (migrations/migrate ["reset"] {:database-url "jdbc:h2:./copa_test;MVCC=true"})
    (f)))

(deftest get-user
  (jdbc/with-db-transaction [t-conn *db*]
                            (jdbc/db-set-rollback-only! t-conn)
                            (testing "get initial admin user"
                              (is (= {:username "admin"
                                      :password passw
                                      :admin    true
                                      :lang     "en"}
                                     (dissoc (db/get-user {:username "admin"}) :user_id))))))

(deftest get-all-users
  (jdbc/with-db-transaction [t-conn *db*]
                            (jdbc/db-set-rollback-only! t-conn)
                            (testing "get all users"
                              (is (= 1 (count (db/get-users t-conn)))))))

(deftest insert-user
  (jdbc/with-db-transaction [t-conn *db*]
                            (jdbc/db-set-rollback-only! t-conn)
                            (testing "inserting new user"
                              (is (= 1 (db/create-user! t-conn
                                                        {:username "newuser"
                                                         :password "pass"
                                                         :admin    false})))
                              (is (= {:username "newuser"
                                      :password "pass"
                                      :admin    false
                                      :lang     "en"}
                                     (dissoc (db/get-user t-conn {:username "newuser"}) :user_id))))
                            (testing "insert another user with same username fails"
                              (is (thrown? SQLException
                                           (db/create-user!
                                             t-conn
                                             {:username "newuser"
                                              :password "pass"
                                              :admin    false}))))))

(deftest delete-user
  (jdbc/with-db-transaction [t-conn *db*]
                            (jdbc/db-set-rollback-only! t-conn)
                            (testing "deleting a user"
                              (is (= 1 (db/create-user! t-conn
                                                        {:username "newuser"
                                                         :password "pass"
                                                         :admin    false})))
                              (is (= 1 (db/delete-user! t-conn
                                                        {:username "newuser"})))
                              (is (= nil
                                     (dissoc (db/get-user t-conn {:username "newuser"}) :user_id))))))

(deftest update-user-password
  (jdbc/with-db-transaction [t-conn *db*]
                            (jdbc/db-set-rollback-only! t-conn)
                            (db/create-user! t-conn
                                             {:username "newuser"
                                              :password "pass"
                                              :admin    false})
                            (db/update-user-password! t-conn
                                                      {:username "newuser"
                                                       :password "p"})
                            (is (= "p"
                                   (:password (db/get-user t-conn {:username "newuser"}))))))

(deftest update-user-lang
  (jdbc/with-db-transaction [t-conn *db*]
                            (jdbc/db-set-rollback-only! t-conn)
                            (db/create-user! t-conn
                                             {:username "newuser"
                                              :password "pass"
                                              :admin    false})
                            (db/update-user-lang! t-conn
                                                  {:username "newuser"
                                                   :lang     "cz"})
                            (is (= "cz"
                                   (:lang (db/get-user t-conn {:username "newuser"}))))))