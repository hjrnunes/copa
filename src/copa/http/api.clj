(ns copa.http.api
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as sc]
            [copa.http.schemas :as schemas]
            [copa.middleware :as middleware]
            [copa.http.service :as s]))

(defapi service-routes
        (swagger-routes
          {:ui   "/swagger-ui"
           :spec "/swagger.json"
           :data {:info {:version     "0.0.1"
                         :title       "COPA"
                         :description "COPA is a Culinary Operating Procedure Adviser"
                         :contact     {:name  "Henrique Nunes"
                                       :email "hjrnunes@gmail.com"
                                       :url   "http://copa.nomax.co"}}
                  :tags [{:name "api", :description "The API"}]}})
        (POST "/auth" []
              :summary "Authenticate user"
              :body-params [username :- String
                            password :- String]
              (s/login username password))
        (context "/api" []
                 :middleware [middleware/wrap-restricted]
                 :tags ["api"]

                 (GET "/settings" []
                      :summary "Get settings"
                      (s/get-settings))

                 (GET "/recipes" []
                      :summary "Get all recipes"
                      (s/get-all-recipes))

                 (POST "/recipes" []
                       :body [body schemas/Recipe]
                       :summary "Create new recipe"
                       (s/create-recipe body))

                 (GET "/recipe" []
                      :query-params [name :- sc/Str]
                      :summary "Get a recipe by name"
                      (s/get-recipe-by-name name))

                 (GET "/ingredients" []
                      :summary "Get all ingredients"
                      (s/get-all-ingredients))

                 (GET "/ingredient" []
                      :query-params [name :- sc/Str]
                      :summary "Get an ingredient by name"
                      (s/get-ingredient-by-name name))))
