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
  [rc/v-box
   :children [[rc/title :level :level1 :underline? true :label "Ingredients"]
              [:ul.list-group
               (for [measurement (:recipe/measurements @recipe)]
                 ^{:key (:db/id measurement)} [measurement-item measurement])]]])

(defn recipe-preparation []
  (fn [{:keys [recipe/name recipe/description recipe/portions recipe/preparation recipe/categories]}]
    [rc/v-box
     :children [[rc/title :level :level1 :underline? true :label name]
                [rc/title :level :level4 :label description]
                [rc/box
                 :child [rc/p preparation]]]]))

(defn recipe-details []
  (let [recipe (subscribe [:state/selected-recipe])]
    (fn []
      (when @recipe
        [rc/h-box
         :children [[recipe-measurements-list recipe]
                    [recipe-preparation @recipe]]]))))


;; recipe creation ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn wired-textbox [{:keys [label form key textarea]
                      :or   [:textarea false]}]
  (let [model (subscribe [:form-state form key])]
    (fn []
      [rc/v-box
       :children
       [[rc/label :label label]
        [(if textarea
           rc/input-textarea
           rc/input-text)
         :model (or @model "")
         :change-on-blur? true
         :on-change #(dispatch [:form-state/update form key %])]]])))

(defn new-measurement [form-key]
  (let [show (subscribe [:form-state form-key :show-new-measurement])]
    (fn []
      [:div
       (when @show
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
                                     :key   :tmp.measurement/ingredient}]
                     [rc/button
                      :label "OK"
                      :class "btn-primary"
                      :on-click #(dispatch [:measurement/add form-key])]]])
       [rc/md-circle-icon-button
        :md-icon-name "zmdi-plus"
        :tooltip "Add another ingredient"
        :on-click #(dispatch [:form-state/update form-key :show-new-measurement true])]])))

(defn measurements [form-key]
  (let [measurements (subscribe [:form-state form-key :recipe/measurements])]
    (fn []
      [:ul.list-group
       (for [[idx measurement] (indexed @measurements)]
         ^{:key idx} [measurement-item measurement])
       [new-measurement form-key]
       ])))

(defn new-recipe []
  (let [form-key :new-recipe]
    [rc/v-box
     :children [[rc/v-box
                 :children [[rc/title :level :level1 :underline? true :label "New Recipe"]
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
                                            :textarea true}]]]
                [rc/v-box
                 :children [[rc/title :level :level2 :underline? true :label "Ingredients"]
                            [measurements form-key]
                            [rc/button
                             :label "Add recipe!"
                             :class "btn-primary"
                             :on-click #(dispatch [:recipe/create form-key])]]]]]))


;; recipe list ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn recipe-list-item []
  (fn [{:keys [db/id recipe/name recipe/description recipe/portions recipe/preparation recipe/categories recipe/measurements]}]
    [:a.list-group-item {:href     "#"
                         :on-click #(dispatch [:recipe/select id])} name]))

(defn recipe-list [recipes]
  [:div.list-group
   (for [recipe @recipes]
     ^{:key (:db/id recipe)} [recipe-list-item recipe])])

(defn recipe-list-menu []
  (let [recipes (subscribe [:data/recipes])]
    (fn []
      [rc/v-box
       :children [[rc/h-box
                   :children [[rc/title :level :level1 :label "Recipes"]
                              [rc/md-circle-icon-button
                               :md-icon-name "zmdi-plus"
                               :tooltip "Add new recipe"
                               :on-click #(dispatch [:state/update :active-pane :new-recipe])]]]
                  (when-not (empty? @recipes)
                    [recipe-list recipes])]])))

(defn loading-status [loading]
  [:div
   (if @loading
     [:span.label.label-warning "Syncing"]
     [:span.label.label-info "Synced"])])

(defn footer []
  (let [loading (subscribe [:state :loading])]
    (fn []
      [rc/h-box
       :children [[loading-status loading]]])))

;; app ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def panes {:recipe-details recipe-details
            :new-recipe     new-recipe})

(defn copa-app []
  (let [active-pane (subscribe [:state :active-pane])]
    (fn []
      [rc/v-box
       :children [[rc/h-box
                   :children [[rc/v-box
                               :children [[recipe-list-menu]]]
                              [rc/v-box
                               :children [(if @active-pane
                                            [(@active-pane panes)]
                                            [rc/box
                                             :child [:div]])]]
                              ]]]])))