(ns copa.db
  (:require [cljs.reader]
            [cljs.spec :as s]))


(s/def ::measurement_id (s/or :int int?
                              :string string?))
(s/def :recipe/ingredient string?)
(s/def ::unit string?)
(s/def ::quantity (s/or :int int?
                        :string string?
                        :double double?))
(s/def ::measurement (s/keys :req-un [::measurement_id :recipe/ingredient]
                             :opt-un [::quantity ::unit]))

(s/def ::recipe_id (s/or :int int?
                         :string string?))
(s/def ::name string?)
(s/def ::description string?)
(s/def ::portions string?)
(s/def ::duration string?)
(s/def ::preparation string?)
(s/def ::source string?)
(s/def ::user string?)
(s/def ::measurements (s/coll-of ::measurement))
(s/def ::recipe (s/keys :req-un [::recipe_id ::name ::user ::preparation]
                        :opt-un [::description ::portions ::source ::measurements ::duration]))


(s/def ::ingredient_id (s/or :int int?
                             :string string?))
(s/def ::ingredient (s/keys :req-un [::ingredient_id ::name]))

(s/def ::username string?)
(s/def ::lang string?)
(s/def ::admin boolean?)

(s/def :data/recipes (s/coll-of ::recipe))
(s/def :data/ingredients (s/coll-of ::ingredient))

(s/def :index/recipes (s/map-of ::recipe_id ::recipe))
(s/def :index/ingredients (s/map-of ::ingredient_id ::ingredient))

(s/def ::data (s/keys :req-un [:data/recipes :data/ingredients]))


(s/def :state/user (s/keys :req-un [::username]
                           :opt-un [::lang ::admin]))

(s/def ::users (s/map-of ::username :state/user))

(s/def ::index (s/keys :req-un [:index/recipes :index/ingredients]
                       :opt-un [::users]))

(s/def ::active-main-pane (s/nilable keyword?))
(s/def ::active-recipe-pane (s/nilable keyword?))
(s/def ::force-login boolean?)
(s/def ::loading int?)
(s/def ::selected-recipe (s/nilable ::recipe_id))
(s/def ::selected-recipe (s/nilable ::user))
(s/def ::token (s/nilable string?))

(s/def ::state (s/keys :req-un [::force-login]
                       :opt-un [:state/user ::active-main-pane ::active-recipe-pane ::loading ::selected-recipe ::token ::selected-recipe]))

(s/def ::db (s/keys :req-un [::data ::index ::state]))


(def default-db
  {:data  {:recipes {} :ingredients {}}
   :index {:recipes {} :ingredients {}}
   :state {:force-login true}})