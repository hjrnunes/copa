(ns copa.test.service.recipes
  (:require [clojure.test :refer :all]
            [luminus-migrations.core :as migrations]
            [mount.core :as mount]
            [copa.http.service :as s]
            [copa.config :refer [env]]
            [copa.db.core :refer [*db*] :as db]))

;(def passw "bcrypt+sha512$a20e2dd70567d58028a5a108ce54b832$12$00a281eb5acd4dd11c37dffadc5f4aeb78288684029268a9")

(use-fixtures
  :each
  (fn [f]
    (mount/start
      #'copa.config/env
      #'copa.db.core/*db*)
    (migrations/migrate ["reset"] {:database-url "jdbc:h2:./copa_test;MVCC=true"})
    (f)
    (db/clean-recipes-table)))

(def sample-recipe {:name         "a recipe"
                    :description  "a description"
                    :portions     "3"
                    :source       "a source"
                    :preparation  "the preparation"
                    :user         "admin"
                    :measurements [{:ingredient "garlic"
                                    :quantity   1
                                    :unit       "g"}
                                   {:ingredient "beer"
                                    :quantity   1}
                                   {:ingredient "salt"}]})

(def sample-recipe-resp {:recipe_id    1
                         :name         "a recipe"
                         :description  "a description"
                         :portions     "3"
                         :source       "a source"
                         :preparation  "the preparation"
                         :user         "admin"
                         :measurements [{:measurement_id 1
                                         :ingredient     "garlic"
                                         :quantity       1.0
                                         :unit           "g"}
                                        {:measurement_id 2
                                         :ingredient     "beer"
                                         :quantity       1.0}
                                        {:measurement_id 3
                                         :ingredient     "salt"}]})


(deftest test-create-recipe
  (testing "create works"
    (let [response (s/create-recipe sample-recipe)
          body (:body response)]
      (is (= 200 (:status response)))
      (is (= sample-recipe-resp
             body))))
  (testing "create works"
    (let [response (s/get-all-recipes)
          body (:body response)]
      (is (= 200 (:status response)))
      (is (= [sample-recipe-resp]
             body)))))

;(testing "login fails"
;  (let [response (s/login "admin" "p")
;        body (:body response)]
;    (is (= 400 (:status response)))
;    (is (= {:message "Login failed. Wrong auth details."}
;           body))))
;)

;(deftest test-get-users
;  (testing "get-users returns all users"
;    (let [response (s/get-users)
;          body (:body response)]
;      (is (= 200 (:status response)))
;      (is (= [{:username "admin"
;               :admin    true
;               :lang     "en"}]
;             body)))))
;
;(deftest test-create-user
;  (let [response (s/create-user {:username "newuser"
;                                 :password "pass"
;                                 :lang     "en"
;                                 :admin    false})
;        body (:body response)]
;    (is (= 200 (:status response)))
;    (is (= {:username "newuser"
;            :admin    false
;            :lang     "en"}
;           body))
;    (is (= (set [{:username "newuser"
;                  :admin    false
;                  :lang     "en"}
;                 {:username "admin"
;                  :admin    true
;                  :lang     "en"}])
;           (set (:body (s/get-users)))))))
;
;(deftest test-delete-user
;  (s/create-user {:username "newuser"
;                  :password "pass"
;                  :lang     "en"
;                  :admin    false})
;  (let [response (s/delete-user "newuser")
;        body (:body response)]
;    (is (= 200 (:status response)))
;    (is (= {:username "newuser"} body))
;    (is (= (set [{:username "admin"
;                  :admin    true
;                  :lang     "en"}])
;           (set (:body (s/get-users)))))))

