(ns copa.views
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]))


(defn recipe-item []
  (fn [{:keys [recipe/name recipe/description recipe/portions recipe/preparation recipe/categories recipe/measurements]}]
    [:li name]))

(defn recipe-list [recipes]
  [:ul.recipe-list
   (for [recipe @recipes]
     ^{:key (:db/id recipe)} [recipe-item recipe])])

(defn copa-app []
  (let [recipes (subscribe [:recipes])]
    (fn []
      [:div
       [:h1 "Recipes"]
       (when-not (empty? @recipes)
         [:div
          [recipe-list recipes]])])))