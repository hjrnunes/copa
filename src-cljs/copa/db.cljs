(ns copa.db
  (:require [cljs.reader]
            [schema.core :as s :include-macros true]))

(s/defschema BaseEntity
  {(s/optional-key :db/id) s/Int})

(s/defschema Measurement
  (merge BaseEntity {:measurement/ingredient            s/Str
                     :measurement/quantity              s/Num
                     (s/optional-key :measurement/unit) s/Str}))

(s/defschema Recipe
  (merge BaseEntity {:recipe/name                         s/Str
                     (s/optional-key :recipe/description) s/Str
                     (s/optional-key :recipe/portions)    s/Int
                     :recipe/preparation                  s/Str
                     (s/optional-key :recipe/categories)  [s/Str]
                     :recipe/measurements                 [Measurement]
                     }))


(s/defschema app-schema
  {:recipes [Recipe]
   :state   {(s/optional-key :loading) s/Bool}})

(def default-value
  {:recipes []
   :state   {}})