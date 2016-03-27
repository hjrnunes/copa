(ns copa.views.ingredients
  (:require-macros [copa.macros :refer [handler-fn]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [re-com.core :as rc :refer-macros [handler-fn]]
            [clojure.string :refer [join capitalize]]
            [plumbing.core :refer [indexed]]
            [copa.views.recipes :refer [recipes-section]]))

(defn ingredients-section []
  (let []
    (fn []
      [:label "Ingredients"])))
