(ns copa.views.ingredients
  (:require-macros [copa.macros :refer [handler-fn]])
  (:require [reagent.core :as r]
            [goog.dom :as dom]
            [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :refer [join capitalize]]
            [plumbing.core :refer [indexed]]
            [copa.views.recipes :refer [recipes-section]]
            [copa.util :refer [t]]))

(defn ingredient-list-item [ingredient]
  [:div.item
   {:on-click (handler-fn (dispatch [:ingredient/select-match (:ingredient_id ingredient)]))}
   [:div.content
    [:div.header
     (capitalize (:name ingredient))]]])

(defn matcher-results []
  (let [matched (subscribe [:matched-recipes])
        lang (subscribe [:lang])]
    (fn []
      (if (not-empty @matched)
        [:div
         [:h5.ui.disabled.dividing.header (str (t @lang :ingredients/matched) " " (count @matched))]
         [:div.ui.list
          (for [recipe @matched]
            ^{:key (:recipe_id recipe)} [:div.item
                                         [:i.food.icon]
                                         [:div.content
                                          [:a.header
                                           {:on-click (handler-fn
                                                        (dispatch [:push-url-for :recipe :id (:recipe_id recipe)]))}
                                           (:name recipe)]
                                          [:div.description (:description recipe)]]])]]
        [:h4.ui.disabled.header (t @lang :ingredients/matched-none)]))))

(defn selected-ingredients-list []
  (let [selected (subscribe [:selected-ingredients])]
    (fn []
      [:div.ui.horizontal.list
       (for [ingredient @selected]
         ^{:key (:ingredient_id ingredient)} [:div.item
                                              [:div.ui.olive.large.label
                                               (:name ingredient)
                                               [:i.delete.icon
                                                {:on-click (handler-fn
                                                             (dispatch [:ingredient/deselect-match (:ingredient_id ingredient)]))}]]])])))


(defn ingredients-details []
  (let [lang (subscribe [:lang])]
    (fn []
      [:div.ui.two.column.relaxed.divided.grid
       [:div.row
        [:div.twelve.wide.column
         [selected-ingredients-list]]]
       [:div.ui.divider]
       [:div.row
        [:div.twelve.wide.column
         [matcher-results]]]])))


(defn ingredient-search [ph-label]
  (let [ingredients (subscribe [:sorted-ingredients])]
    (fn []
      [:div.ui.right.align.search.item
       [:div.ui.transparent.icon.input
        [:input#ingsearch.prompt {:placeholder ph-label
                                  :on-change   (handler-fn
                                                 (.. (js/$ ".ui.search")
                                                     (search (clj->js {:source       @ingredients
                                                                       :searchFields ["name"]
                                                                       :fields       {"title" "name"}
                                                                       :onSelect     (fn [result _]
                                                                                       (set! (.-value (dom/getElement "ingsearch")) nil) ;todo doesn't work
                                                                                       (dispatch [:ingredient/select-match (:ingredient_id (js->clj result :keywordize-keys true))]))}))))}]
        [:i.search.link.icon]]
       [:div.results]])))

(defn ingredients-section []
  (let [ingredients (subscribe [:sorted-ingredients])
        lang (subscribe [:lang])]
    (fn []
      [:div
       [:div.ui.top.attached.menu
        [:div.right.menu
         [ingredient-search (t @lang :ingredients/menu-search-ph)]]]
       [:div.ui.bottom.attached.segment
        [:div.ui.two.column.relaxed.divided.grid
         [:div.four.wide.column
          [:div.ui.middle.aligned.selection.list
           (for [ingredient @ingredients]
             ^{:key (:ingredient_id ingredient)} [ingredient-list-item ingredient])
           ]]
         [:div.twelve.wide.column
          [ingredients-details]]]]])))