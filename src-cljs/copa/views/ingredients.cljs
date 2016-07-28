(ns copa.views.ingredients
  (:require-macros [copa.macros :refer [handler-fn]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :refer [join capitalize]]
            [plumbing.core :refer [indexed]]
            [copa.views.recipes :refer [recipes-section]]))

(defn ingredient-details []
  (let [ingredient (subscribe [:state/selected-ingredient])]
    (fn []
      (when @ingredient
        [:div
         [:div.ui.medium.header
          (capitalize (:name @ingredient))]
         [:div.ui.segment
          [:div.ui.top.attached.basic.label
           "Entra nas seguintes receitas:"]
          [:div.ui.list
           (for [[idx recipe] (indexed (:recipes @ingredient))]
             ^{:key idx} [:div.item
                          [:div.content
                           [:a.header
                            {:on-click (handler-fn
                                         (dispatch [:recipe/select recipe])
                                         (dispatch [:state/update :active-main-pane :recipes])
                                         (dispatch [:state/update :active-recipe-pane :recipe-list]))}
                            recipe]]])]]]))))

(defn ingredient-list-item [ingredient]
  [:div.item
   {:on-click #(dispatch [:ingredient/select (:name ingredient)])}
   [:div.content
    [:div.header
     (capitalize (:name ingredient))]]])

(defn ingredient-list []
  (let [ingredients (subscribe [:sorted/ingredients])]
    (fn []
      [:div.ui.middle.aligned.selection.list
       (for [ingredient @ingredients]
         ^{:key (:_id ingredient)} [ingredient-list-item ingredient])])))

(defn ingredients-section []
  (let []
    (fn []
      [:div.ui.two.column.relaxed.divided.grid
       [:div.row
        [:div.two.wide.column
         [ingredient-list]]
        [:div.fourteen.wide.column
         [ingredient-details]]]])))
