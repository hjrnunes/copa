(ns copa.views
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]
            [re-com.buttons :refer [button]]
            [re-com.core :refer [title]]))

(defn measurement-item []
  (fn [{:keys [db/id measurement/ingredient measurement/quantity measurement/unit]}]
    [:li.list-group-item quantity unit (:ingredient/name ingredient)]))

(defn recipe-measurements-list [recipe]
  [:div
   [title :level :level1 :underline? true :label "Ingredients"]
   [:ul.list-group
      (for [measurement (:recipe/measurements @recipe)]
        ^{:key (:db/id measurement)} [measurement-item measurement])]])

(defn recipe-preparation []
  (fn [{:keys [recipe/name recipe/description recipe/portions recipe/preparation recipe/categories]}]
    [:div.panel.panel-default
     [:div.panel-body
      [title :level :level1 :underline? true :label name]
      [title :level :level4 :label description]
      preparation]]))

(defn recipe-details []
  (let [recipe (subscribe [:selected-recipe])]
    (fn []
      (when @recipe
        [:div.row
         [:div.col-md-3
          [:div
           [recipe-measurements-list recipe]]]
         [:div.col-md-9
          [recipe-preparation @recipe]]]))))

(defn recipe-list-item []
  (fn [{:keys [db/id recipe/name recipe/description recipe/portions recipe/preparation recipe/categories recipe/measurements]}]
    [:a.list-group-item {:href     "#"
                         :on-click #(dispatch [:select-recipe id])} name]))

(defn recipe-list [recipes]
  [:div.list-group
   (for [recipe @recipes]
     ^{:key (:db/id recipe)} [recipe-list-item recipe])])

(defn copa-app []
  (let [recipes (subscribe [:recipes])]
    (fn []
      [:div.container
       [:div.row
        [:div.col-md-3
         [title :level :level1 :underline? true :label "Recipes"]
         (when-not (empty? @recipes)
           [:div
            [recipe-list recipes]])]
        [:div.col-md-9
         [recipe-details]
         ]]])))