(ns copa.test.service.recipes
  (:require [clojure.test :refer :all]
            [luminus-migrations.core :as migrations]
            [mount.core :as mount]
            [copa.http.service :as s]
            [copa.config :refer [env]]
            [copa.db.core :refer [*db*] :as db]
            [clojure.pprint :refer [pprint]])
  (:import (clojure.lang ExceptionInfo)))

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

(def updt-recipe (-> sample-recipe-resp
                     (assoc :portions "5")
                     (assoc :measurements [{:measurement_id 4
                                            :ingredient     "garlic"
                                            :quantity       3.0
                                            :unit           "g"}
                                           {:ingredient     "flower"
                                            :measurement_id 5}])))


(deftest test-create-recipe
  (testing "create works"
    (let [response (s/create-recipe sample-recipe)
          body (:body response)]
      (is (= 200 (:status response)))
      (is (= sample-recipe-resp body))))

  (testing "create with same name fails with right exception"
    (is (thrown-with-msg? ExceptionInfo
                          #"Recipe name already exists"
                          (s/create-recipe sample-recipe))))

  (testing "get all recipes works"
    (let [response (s/get-all-recipes)
          body (:body response)]
      (is (= 200 (:status response)))
      (is (= [sample-recipe-resp] body))))

  (testing "get recipe by name works"
    (let [response (s/get-recipe-by-name "a recipe")
          body (:body response)]
      (is (= 200 (:status response)))
      (is (= sample-recipe-resp body))))

  (testing "delete recipe by id works"
    (let [response (s/delete-recipe-by-id 1)
          body (:body response)]
      (is (= 200 (:status response)))
      (is (= 1 body)))))

(deftest test-update-recipe
  (testing "update works"
    (s/create-recipe sample-recipe)
    (pprint updt-recipe)
    (let [response (s/create-recipe updt-recipe)
          body (:body response)]
      (is (= 200 (:status response)))
      (is (= updt-recipe body)))))


