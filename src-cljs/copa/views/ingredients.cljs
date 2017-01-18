(ns copa.views.ingredients
  (:require-macros [copa.macros :refer [handler-fn]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :refer [join capitalize]]
            [plumbing.core :refer [indexed]]
            [copa.views.recipes :refer [recipes-section]]
            [copa.util :refer [t]]))

(defn ingredient-details []
  (let [ingredient (subscribe [:selected-ingredient])
        recipes-index (subscribe [:index/recipes])
        lang (subscribe [:lang])]
    (fn []
      (when @ingredient
        [:div
         [:div.ui.medium.header
          (capitalize (:name @ingredient))]
         [:div.ui.segment
          [:div.ui.top.attached.basic.label
           (t @lang :ingredients/is-in-recipes)]
          [:div.ui.list
           (doall (for [[idx recipe] (indexed (:recipes @ingredient))]
                    ^{:key idx} [:div.item
                                 [:div.content
                                  [:a.header
                                   {:on-click (handler-fn
                                                (dispatch [:push-url-for :recipe :id recipe]))}
                                   (:name (get @recipes-index recipe))]]]))]]]))))

(defn ingredient-list-item [ingredient]
  [:div.item
   {:on-click (handler-fn (dispatch [:push-url-for :ingredient :id (:ingredient_id ingredient)]))}
   [:div.content
    [:div.header
     (capitalize (:name ingredient))]]])

(defn ingredient-list []
  (let [ingredients (subscribe [:sorted-ingredients])]
    (fn []
      [:div.ui.middle.aligned.selection.list
       (for [ingredient @ingredients]
         ^{:key (:ingredient_id ingredient)} [ingredient-list-item ingredient])])))

(defn ingredients-section []
  (let []
    (fn []
      [:div.ui.two.column.relaxed.divided.grid
       [:div.row
        [:div.two.wide.column
         [ingredient-list]]
        [:div.fourteen.wide.column
         [ingredient-details]]]])))
