(ns copa.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [copa.routes.schemas :as schemas]
            [copa.db.core :refer [db]]
            [copa.db.query :as q]))

(defn get-all-recipes []
  (ok (q/pull-all-recipes db)))

(defn get-all-ingredients []
  (ok (q/pull-all-ingredients db)))

(defn get-recipe-by-name [name]
  (ok (q/pull-recipe-by-name db name)))

(defn get-ingredient-by-name [name]
  (ok (q/pull-ingredient-by-name db name)))

(defapi service-routes
        (ring.swagger.ui/swagger-ui
          "/swagger-ui")
        ;JSON docs available at the /swagger.json route
        (swagger-docs
          {:info {:title "Copa API"}})
        (context* "/api" []
                  :tags ["thingie"]

                  (GET* "/recipes" []
                        ;:return [schemas/Recipe]
                        :summary "Get all recipes"
                        (get-all-recipes))

                  (GET* "/recipe" []
                        :query-params [name :- s/Str]
                        ;:return schemas/Recipe
                        :summary "Get a recipe by name"
                        (get-recipe-by-name name))

                  (GET* "/ingredients" []
                        ;:return [schemas/Recipe]
                        :summary "Get all ingredients"
                        (get-all-ingredients))

                  (GET* "/ingredient" []
                        :query-params [name :- s/Str]
                        ;:return schemas/Recipe
                        :summary "Get an ingredient by name"
                        (get-ingredient-by-name name))

                  ;(GET* "/plus" []
                  ;      :return       Long
                  ;      :query-params [x :- Long, {y :- Long 1}]
                  ;      :summary      "x+y with query-parameters. y defaults to 1."
                  ;      (ok (+ x y)))
                  ;
                  ;(POST* "/minus" []
                  ;       :return      Long
                  ;       :body-params [x :- Long, y :- Long]
                  ;       :summary     "x-y with body-parameters."
                  ;       (ok (- x y)))
                  ;
                  ;(GET* "/times/:x/:y" []
                  ;      :return      Long
                  ;      :path-params [x :- Long, y :- Long]
                  ;      :summary     "x*y with path-parameters"
                  ;      (ok (* x y)))
                  ;
                  ;(POST* "/divide" []
                  ;       :return      Double
                  ;       :form-params [x :- Long, y :- Long]
                  ;       :summary     "x/y with form-parameters"
                  ;       (ok (/ x y)))
                  ;
                  ;(GET* "/power" []
                  ;      :return      Long
                  ;      :header-params [x :- Long, y :- Long]
                  ;      :summary     "x^y with header-parameters"
                  ;      (ok (long (Math/pow x y))))
                  ;
                  ;(PUT* "/echo" []
                  ;      :return   [{:hot Boolean}]
                  ;      :body     [body [{:hot Boolean}]]
                  ;      :summary  "echoes a vector of anonymous hotties"
                  ;      (ok body))
                  ;
                  ;(POST* "/echo" []
                  ;       :return   (s/maybe Thingie)
                  ;       :body     [thingie (s/maybe Thingie)]
                  ;       :summary  "echoes a Thingie from json-body"
                  ;       (ok thingie))

                  )

        (context* "/context" []
                  :tags ["context*"]
                  :summary "summary inherited from context"
                  (context* "/:kikka" []
                            :path-params [kikka :- s/Str]
                            :query-params [kukka :- s/Str]
                            (GET* "/:kakka" []
                                  :path-params [kakka :- s/Str]
                                  (ok {:kikka kikka
                                       :kukka kukka
                                       :kakka kakka})))))
