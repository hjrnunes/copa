(ns copa.http.api
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as sc]
            [copa.http.schemas :as schemas]
            [copa.http.service :as s]))

(defapi service-routes
        (ring.swagger.ui/swagger-ui
          "/swagger-ui")
        ;JSON docs available at the /swagger.json route
        (swagger-docs
          {:info {:title "Copa API"}})
        (context* "/api" []
                  :tags ["api"]

                  (GET* "/settings" []
                        ;:return [schemas/Recipe]
                        :summary "Get settings"
                        (s/get-settings))

                  (GET* "/recipes" []
                        ;:return [schemas/Recipe]
                        :summary "Get all recipes"
                        (s/get-all-recipes))

                  (POST* "/recipes" []
                         ;:return schemas/Recipe
                         :body [body schemas/Recipe]
                         :summary "Create new recipe"
                         (s/create-recipe body))

                  (GET* "/recipe" []
                        :query-params [name :- sc/Str]
                        ;:return schemas/Recipe
                        :summary "Get a recipe by name"
                        (s/get-recipe-by-name name))

                  (GET* "/ingredients" []
                        ;:return [schemas/Recipe]
                        :summary "Get all ingredients"
                        (s/get-all-ingredients))

                  (GET* "/ingredient" []
                        :query-params [name :- sc/Str]
                        ;:return schemas/Recipe
                        :summary "Get an ingredient by name"
                        (s/get-ingredient-by-name name))))
