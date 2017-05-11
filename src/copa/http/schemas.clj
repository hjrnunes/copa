(ns copa.http.schemas
  (:require [schema.core :as s]
            [schema.coerce :as sc]
            [ring.swagger.schema :as rs :refer [describe]]))

(s/defschema BaseEntity
  {(s/optional-key :recipe_id) (describe s/Str "Database id")})

(s/defschema Measurement
  (merge BaseEntity {:ingredient                (describe s/Str "Measurement ingredient")
                     (s/optional-key :quantity) (describe Double "Measurement quantity")
                     (s/optional-key :unit)     (describe s/Str "Measurement unit")}))

(s/defschema Recipe
  (merge BaseEntity {:name                         (describe s/Str "Recipe's name")
                     (s/optional-key :description) (describe s/Str "Recipe's description")
                     (s/optional-key :portions)    (describe s/Str "Recipe's portions")
                     (s/optional-key :source)      (describe s/Str "Recipe's source")
                     (s/optional-key :duration)    (describe s/Str "Recipe's duration")
                     :preparation                  (describe s/Str "Recipe's preparation")
                     (s/optional-key :categories)  (describe [s/Str] "Recipe's categories")
                     :measurements                 (describe [Measurement] "Recipe's measurements")
                     :user                         (describe s/Str "Recipe's user")}))

(s/defschema User
  (merge BaseEntity {:username (describe s/Str "User name")
                     :password (describe s/Str "User password")
                     :admin    (describe s/Bool "Is the user an admin?")}))

(s/defschema UserLang
  (merge BaseEntity {:username (describe s/Str "User name")
                     :lang     (describe s/Str "Language preference")}))

(s/defschema UserPassword
  (merge BaseEntity {:username (describe s/Str "User name")
                     :current  (describe s/Str "Current password")
                     :new      (describe s/Str "New password")
                     :confirm  (describe s/Str "New password confirmation")}))

