(ns copa.routes.schemas
  (:require [schema.core :as s]
            [schema.coerce :as sc]
            [ring.swagger.schema :as rs :refer [describe]]))

(s/defschema BaseEntity
  {(s/optional-key :db/id) (describe Long "Database id")})

(s/defschema Ingredient
  (merge BaseEntity {:ingredient/name (describe s/Str "Ingredient name")}))

(s/defschema Measurement
  (merge BaseEntity {:measurement/ingredient            (s/conditional map? (describe Ingredient "Measurement ingredient")
                                                                       :else (describe s/Str "Measurement ingredient"))
                     :measurement/quantity              (describe Double "Measurement quantity")
                     (s/optional-key :measurement/unit) (describe s/Str "Measurement unit")}))

(s/defschema Recipe
  (merge BaseEntity {:recipe/name                         (describe s/Str "Recipe's name")
                     (s/optional-key :recipe/description) (describe s/Str "Recipe's description")
                     (s/optional-key :recipe/portions)    (describe Long "Recipe's portions")
                     :recipe/preparation                  (describe s/Str "Recipe's preparation")
                     (s/optional-key :recipe/categories)  (describe [s/Str] "Recipe's categories")
                     :recipe/measurements                 (describe [Measurement] "Recipe's measurements")
                     }))

(s/defschema BaseCopy
  {:src_collection    (describe s/Str "Source collection")
   :src_db            (describe s/Str "Source database")
   :target_collection (describe s/Str "Target collection")
   :target_db         (describe s/Str "Target database")})

