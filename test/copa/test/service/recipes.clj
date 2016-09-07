(ns copa.test.service.recipes
  (:require [clojure.test :refer :all]
            [luminus-migrations.core :as migrations]
            [mount.core :as mount]
            [copa.http.service :as s]
            [copa.config :refer [env]]))

;(def passw "bcrypt+sha512$a20e2dd70567d58028a5a108ce54b832$12$00a281eb5acd4dd11c37dffadc5f4aeb78288684029268a9")

(use-fixtures
  :each
  (fn [f]
    (mount/start
      #'copa.config/env
      #'copa.db.core/*db*)
    (migrations/migrate ["reset"] {:database-url "jdbc:h2:./copa_test;MVCC=true"})
    (f)))




(deftest test-login
  (testing "login works"
    (let [response (s/login "admin" "admin")
          body (:body response)]
      (is (= 200 (:status response)))
      (is (= {:username "admin"
              :admin    true
              :lang     "en"}
             (:user body)))
      (is (contains? body :token))))

  (testing "login fails"
    (let [response (s/login "admin" "p")
          body (:body response)]
      (is (= 400 (:status response)))
      (is (= {:message "Login failed. Wrong auth details."}
             body)))))

(deftest test-get-users
  (testing "get-users returns all users"
    (let [response (s/get-users)
          body (:body response)]
      (is (= 200 (:status response)))
      (is (= [{:username "admin"
               :admin    true
               :lang     "en"}]
             body)))))

(deftest test-create-user
  (let [response (s/create-user {:username "newuser"
                                 :password "pass"
                                 :lang     "en"
                                 :admin    false})
        body (:body response)]
    (is (= 200 (:status response)))
    (is (= {:username "newuser"
            :admin    false
            :lang     "en"}
           body))
    (is (= (set [{:username "newuser"
                  :admin    false
                  :lang     "en"}
                 {:username "admin"
                  :admin    true
                  :lang     "en"}])
           (set (:body (s/get-users)))))))

(deftest test-delete-user
  (s/create-user {:username "newuser"
                  :password "pass"
                  :lang     "en"
                  :admin    false})
  (let [response (s/delete-user "newuser")
        body (:body response)]
    (is (= 200 (:status response)))
    (is (= {:username "newuser"} body))
    (is (= (set [{:username "admin"
                  :admin    true
                  :lang     "en"}])
           (set (:body (s/get-users)))))))

