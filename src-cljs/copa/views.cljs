(ns copa.views
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]
            [re-com.core :as rc]
            [clojure.string :refer [join capitalize]]
            [plumbing.core :refer [indexed]]))

;; recipe details ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn compose-measurement [ingredient quantity unit]
  (if unit
    [:span [:span.label.label-info quantity] [:span.label.label-warning (capitalize unit)] (join " " [" " "of" (capitalize ingredient)])]
    [:span (join " " [quantity (capitalize ingredient)])]))

(defn measurement-item []
  (fn [{:keys [db/id measurement/ingredient measurement/quantity measurement/unit]}]
    [:li.list-group-item (compose-measurement (get ingredient :ingredient/name ingredient) quantity unit)]))

(defn recipe-measurements-list [recipe]
  [:div
   [rc/title :level :level1 :underline? true :label "Ingredients"]
   [:ul.list-group
    (for [measurement (:recipe/measurements @recipe)]
      ^{:key (:db/id measurement)} [measurement-item measurement])]])

(defn recipe-preparation []
  (fn [{:keys [recipe/name recipe/description recipe/portions recipe/preparation recipe/categories]}]
    [:div.panel.panel-default
     [:div.panel-body
      [rc/title :level :level1 :underline? true :label name]
      [rc/title :level :level4 :label description]
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


;; recipe creation ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn wired-textbox [{:keys [label form key textarea]
                      :or   [:textarea false]}]
  (let [model (subscribe [:form-input form key])]
    (fn []
      [rc/v-box
       :children
       [[rc/label :label label]
        [(if textarea
           rc/input-textarea
           rc/input-text)
         :model (or @model "")
         :change-on-blur? true
         :on-change #(dispatch [:form-input-changed form key %])]]])))

(defn new-measurement [form-key]
  [:div
   [rc/h-box
    :children [
               [wired-textbox {:label "Quantity"
                               :form  form-key
                               :key   :tmp.measurement/quantity}]
               [wired-textbox {:label "Unit"
                               :form  form-key
                               :key   :tmp.measurement/unit}]
               [wired-textbox {:label "Ingredient"
                               :form  form-key
                               :key   :tmp.measurement/ingredient}]]]
   [rc/md-circle-icon-button
    :md-icon-name "zmdi-plus"
    :tooltip "Add another ingredient"
    :on-click #(dispatch [:add-new-measurement form-key])]])

(defn measurements [form-key]
  (let [measurements (subscribe [:form-input form-key :recipe/measurements])]
    (fn []
      [:ul.list-group
       (for [[idx measurement] (indexed @measurements)]
         ^{:key idx} [measurement-item measurement])
       [new-measurement form-key]
       ])))

(defn new-recipe []
  (let [form-key :new-recipe]
    [:dev.row
     [:div.col-md-12
      [rc/title :level :level1 :underline? true :label "New Recipe"]
      [wired-textbox {:label "Name"
                      :form  form-key
                      :key   :recipe/name}]
      [wired-textbox {:label    "Description"
                      :form     form-key
                      :key      :recipe/description
                      :textarea true}]
      [wired-textbox {:label "Portions"
                      :form  form-key
                      :key   :recipe/portions}]
      [wired-textbox {:label    "Preparation"
                      :form     form-key
                      :key      :recipe/preparation
                      :textarea true}]
      [rc/title :level :level2 :underline? true :label "Ingredients"]
      [measurements form-key]
      [rc/button
       :label "Add recipe!"
       :class "btn-primary"
       :on-click #(dispatch [:create-recipe form-key])]]]))


;; recipe list ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn recipe-list-item []
  (fn [{:keys [db/id recipe/name recipe/description recipe/portions recipe/preparation recipe/categories recipe/measurements]}]
    [:a.list-group-item {:href     "#"
                         :on-click #(dispatch [:select-recipe id])} name]))

(defn recipe-list [recipes]
  [:div.list-group
   (for [recipe @recipes]
     ^{:key (:db/id recipe)} [recipe-list-item recipe])])

(defn recipe-list-menu []
  (let [recipes (subscribe [:recipes])]
    (fn []
      [:div
       [rc/title :level :level1 :underline? true :label "Recipes"]
       (when-not (empty? @recipes)
         [:div
          [recipe-list recipes]])
       [:div.pull-right
        [rc/md-circle-icon-button
         :md-icon-name "zmdi-plus"
         :tooltip "Add new recipe"
         :on-click #(dispatch [:display-pane :new-recipe])]]])))

;; app ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def panes {:recipe-details recipe-details
            :new-recipe     new-recipe})

(defn copa-app []
  (let [active-pane (subscribe [:active-pane])]
    (fn []
      [:div.container-fluid
       [:div.row
        [:div.col-md-3
         [recipe-list-menu]
         ]
        [:div.col-md-9
         (when @active-pane
           [(@active-pane panes)])
         ]]])))