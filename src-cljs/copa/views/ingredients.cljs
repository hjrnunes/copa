(ns copa.views.ingredients
  (:require-macros [copa.macros :refer [handler-fn]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [re-com.core :as rc :refer-macros [handler-fn]]
            [clojure.string :refer [join capitalize]]
            [plumbing.core :refer [indexed]]
            [copa.views.recipes :refer [recipes-section]]))

(defn ingredient-recipe-list [ingredient]
  [rc/v-box
   :children [[rc/box
               :style {:padding "1em"}
               :child [rc/title :level :level3 :label "existe nas receitas:"]]
              [rc/v-box
               :style {:margin-left "1em"}
               :children [(for [[idx recipe] (indexed (:recipes @ingredient))]
                            ^{:key idx} [rc/hyperlink
                                         :label recipe
                                         :on-click (handler-fn
                                                     (dispatch [:recipe/select recipe])
                                                     (dispatch [:state/update :active-main-pane :recipes])
                                                     (dispatch [:state/update :active-recipe-pane :recipe-details]))])]]]])

(defn ingredient-details []
  (let [ingredient (subscribe [:state/selected-ingredient])]
    (fn []
      (when @ingredient
        [rc/v-box
         :children [[rc/v-box
                     :children [[rc/h-box
                                 :align :center
                                 :children [[rc/box
                                             :child [rc/title :level :level1 :underline? true :label (capitalize (:name @ingredient))]]]]]]
                    [ingredient-recipe-list ingredient]]]))))

;; recipe list ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ingredient-list-item []
  (let [mouse-over? (r/atom false)
        selected-ingredient (subscribe [:state/selected-ingredient])]
    (fn [{:keys [_id name recipes]}]
      [rc/border
       :r-border "1px solid lightgrey"
       :child [rc/box
               :style {:padding          "1em"
                       :background-color (if (or (= _id (:_id @selected-ingredient))
                                                 @mouse-over?)
                                           "#eee")}
               :attr {:on-click      #(dispatch [:ingredient/select name])
                      :on-mouse-over (handler-fn (reset! mouse-over? true))
                      :on-mouse-out  (handler-fn (reset! mouse-over? false))}
               :child [rc/label :label name]]])))

(defn ingredient-list [ingredients]
  [rc/v-box
   :children (for [ingredient @ingredients]
               ^{:key (:_id ingredient)} [ingredient-list-item ingredient])])

(defn ingredient-list-menu []
  (let [ingredients (subscribe [:sorted/ingredients])]
    (fn []
      [rc/v-box
       :children [[rc/h-box
                   :gap "1"
                   :align :center
                   :children [[rc/box
                               :child [rc/title :level :level1 :label "Ingredientes"]]]]
                  (when-not (empty? @ingredients)
                    [ingredient-list ingredients])]])))

(def ingredient-panes {:ingredient-details ingredient-details})

(defn ingredients-section []
  (let [active-ingredient-pane (subscribe [:state :active-ingredient-pane])]
    (fn []
      [rc/h-box
       :size "1"
       :justify :around
       :gap "4em"
       :children [[rc/v-box
                   :size "2"
                   :children [[ingredient-list-menu]]]
                  [rc/v-box
                   :size "8"
                   :children [(if @active-ingredient-pane
                                [(@active-ingredient-pane ingredient-panes)]
                                [rc/box
                                 :child [:div]])]]]])))
