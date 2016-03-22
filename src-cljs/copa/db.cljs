(ns copa.db
  (:require [cljs.reader]
            [schema.core :as s :include-macros true]))

(s/defschema BaseEntity
  {(s/optional-key :_id) s/Int})

(s/defschema Measurement
  (merge BaseEntity {:ingredient            s/Str
                     :quantity              s/Num
                     (s/optional-key :unit) s/Str}))

(s/defschema Recipe
  (merge BaseEntity {:name                         s/Str
                     (s/optional-key :description) s/Str
                     (s/optional-key :portions)    s/Int
                     :preparation                  s/Str
                     (s/optional-key :categories)  [s/Str]
                     :measurements                 [Measurement]
                     }))


(s/defschema app-schema
  {:recipes [Recipe]
   :index   {s/Int Recipe}
   :state   {(s/optional-key :loading)         s/Bool
             (s/optional-key :selected-recipe) s/Int
             (s/optional-key :active-pane)     s/Keyword}})

(def default-value
  {:recipes []
   :index   {}
   :state   {:active-pane :none}})