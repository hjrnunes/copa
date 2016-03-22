(ns copa.http.schemas
  (:require [schema.core :as s]
            [schema.coerce :as sc]
            [ring.swagger.schema :as rs :refer [describe]]))

(s/defschema BaseEntity
  {(s/optional-key :_id) (describe s/Str "Database id")})

(s/defschema Measurement
  (merge BaseEntity {:ingredient            (describe s/Str "Measurement ingredient")
                     :quantity              (describe Double "Measurement quantity")
                     (s/optional-key :unit) (describe s/Str "Measurement unit")}))

(s/defschema Recipe
  (merge BaseEntity {:name                         (describe s/Str "Recipe's name")
                     (s/optional-key :description) (describe s/Str "Recipe's description")
                     (s/optional-key :portions)    (describe Long "Recipe's portions")
                     :preparation                  (describe s/Str "Recipe's preparation")
                     (s/optional-key :categories)  (describe [s/Str] "Recipe's categories")
                     :measurements                 (describe [Measurement] "Recipe's measurements")}))


