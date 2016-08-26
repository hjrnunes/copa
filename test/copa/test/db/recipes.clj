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
    (migrations/migrate ["reset"] {:database-url "jdbc:h2:./copa_test;MVCC=true"})
    (db/create-user! {:username "newuser"
                      :password "pass"
                      :admin    false})
    (f)))

(def sample-recipe {:name        "a recipe"
                    :description "a description"
                    :portions    "3"
                    :source      "a source"
                    :preparation "the preparation"
                    :user        "newuser"})

;(deftest get-all-users
;  (jdbc/with-db-transaction [t-conn *db*]
;                            (jdbc/db-set-rollback-only! t-conn)
;                            (testing "get all users"
;                              (is (= 1 (count (db/get-users t-conn)))))))

(deftest get-recipe
  (jdbc/with-db-transaction [t-conn *db*]
                            (jdbc/db-set-rollback-only! t-conn)
                            (let [res (db/create-recipe! t-conn sample-recipe)
                                  id (get res (keyword "scope_identity()"))]
                              (testing "get a recipe by id"
                                (is (= sample-recipe
                                       (dissoc (db/get-recipe t-conn {:recipe_id id}) :recipe_id))))
                              (testing "get a recipe by name"
                                (is (= sample-recipe
                                       (dissoc (db/get-recipe-by-name t-conn {:name "a recipe"}) :recipe_id)))))))

(deftest insert-recipe
  (jdbc/with-db-transaction [t-conn *db*]
                            (jdbc/db-set-rollback-only! t-conn)
                            (testing "inserting new recipe"
                              (db/create-recipe! t-conn sample-recipe)
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

(deftest delete-recipe
  (jdbc/with-db-transaction [t-conn *db*]
                            (jdbc/db-set-rollback-only! t-conn)
                            (testing "deleting a recipe"
                              (let [res (db/create-recipe! t-conn sample-recipe)
                                    id (get res (keyword "scope_identity()"))]
                                (is (= 1 (db/delete-recipe! t-conn {:recipe_id id})))
                                (is (= nil (db/get-recipe t-conn {:recipe_id id})))))))


(deftest update-recipe
  (jdbc/with-db-transaction [t-conn *db*]
                            (jdbc/db-set-rollback-only! t-conn)
                            (let [res (db/create-recipe! t-conn sample-recipe)
                                  id (get res (keyword "scope_identity()"))
                                  updted (assoc sample-recipe :name "another name" :recipe_id id)]
                              (is (= 1 (db/update-recipe! t-conn updted)))
                              (is (= updted (db/get-recipe t-conn {:recipe_id id}))))))
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
