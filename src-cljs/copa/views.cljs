(ns copa.views
  (:require-macros [copa.macros :refer [handler-fn]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [re-com.core :as rc :refer-macros [handler-fn]]
            [clojure.string :refer [join capitalize]]
            [plumbing.core :refer [indexed]]
            [json-html.core :refer [edn->hiccup]]))

;; recipe details ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn compose-measurement [{:keys [ingredient quantity unit] :as measurement} edit? mouse-over? form-key]
  [rc/h-box
   :gap "0.2em"
   :align :center
   :children [[rc/label :label (capitalize ingredient)]
              [rc/gap :size "1em"]
              [rc/label :label quantity :class "label label-info"]
              (when unit
                [rc/label :label (capitalize unit) :class "label label-warning"])
              (when (and edit? @mouse-over?)
                [rc/md-icon-button
                 :md-icon-name "zmdi-delete"
                 :tooltip "Delete"
                 :size :smaller
                 :on-click #(dispatch [:measurement/remove form-key measurement])])]])

(defn measurement-item []
  (let [mouse-over? (r/atom false)]
    (fn [{:keys [_id ingredient quantity unit] :as measurement}]
      [rc/box
       :style {:padding          "0.3em"
               :background-color (if @mouse-over? "#eee")}
       :attr {:on-mouse-over (handler-fn (reset! mouse-over? true))
              :on-mouse-out  (handler-fn (reset! mouse-over? false))}
       :child (compose-measurement measurement false mouse-over? nil)])))

(defn recipe-measurements-list [recipe]
  [rc/v-box
   :children [[rc/box
               :style {:padding "1em"}
               :child [rc/title :level :level3 :label "Ingredients"]]
              [rc/border
               :l-border "1px solid lightgrey"
               :child [rc/v-box
                       :style {:margin-left "1em"}
                       :children [(for [[idx measurement] (indexed (:measurements @recipe))]
                                    ^{:key idx} [measurement-item measurement false])]]]]])

(defn recipe-preparation []
  (fn [{:keys [name description portions preparation categories]}]
    [rc/v-box
     :gap "2em"
     :children [[rc/h-box
                 :gap "1"
                 :children [[rc/title :level :level4 :label description]
                            (when portions
                              [rc/title :level :level4 :label (join " " [portions "portions"])])]]
                [rc/v-box
                 :gap "1em"
                 :children [[rc/title :level :level3 :label "Preparation"]
                            [rc/p preparation]]]]]))

(defn recipe-details-header []
  (fn [{:keys [name description portions preparation categories] :as recipe}]
    [rc/v-box
     :children [[rc/h-box
                 ;:gap "1"
                 :align :center
                 :children [[rc/box
                             :child [rc/title :level :level1 :underline? true :label name]]
                            [rc/md-circle-icon-button
                             :style {:margin-left "1em"
                                     :margin-top  "1em"}
                             :md-icon-name "zmdi-edit"
                             :tooltip "Edit recipe"
                             :on-click (handler-fn
                                         (dispatch [:form-state/load :edit-recipe recipe])
                                         (dispatch [:state/update :active-pane :edit-recipe]))]]]]]))

(defn recipe-details []
  (let [recipe (subscribe [:state/selected-recipe])]
    (fn []
      (when @recipe
        [rc/v-box
         :children [[recipe-details-header @recipe]
                    [rc/h-box
                     :gap "2em"
                     :children [[recipe-preparation @recipe]
                                [recipe-measurements-list recipe]]]]]))))


;; recipe creation ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn wired-textbox [{:keys [label form key textarea width]
                      :or   [:textarea false :width "250px"]}]
  (let [model (subscribe [:form-state form key])]
    (fn []
      [rc/v-box
       :children
       [[rc/label :label label]
        [(if textarea
           rc/input-textarea
           rc/input-text)
         :width width
         :model (str (or @model ""))
         :change-on-blur? true
         :on-change #(dispatch [:form-state/update form key %])]]])))

(defn new-measurement [form-key]
  (let [show (subscribe [:form-state form-key :show-new-measurement])]
    (fn []
      [rc/v-box
       :children [(if @show
                    [rc/h-box
                     :align :end
                     :children [[wired-textbox {:label "Quantity"
                                                :form  form-key
                                                :key   :tmp.measurement/quantity
                                                :width "60px"}]
                                [rc/gap :size "0.5em"]
                                [wired-textbox {:label "Unit"
                                                :form  form-key
                                                :key   :tmp.measurement/unit
                                                :width "50px"}]
                                [rc/gap :size "1em"]
                                [wired-textbox {:label "Ingredient"
                                                :form  form-key
                                                :key   :tmp.measurement/ingredient
                                                :width "150px"}]
                                [rc/gap :size "0.5em"]
                                [rc/button
                                 :label "OK"
                                 :class "btn-primary"
                                 :on-click #(dispatch [:measurement/add form-key])]
                                [rc/gap :size "1px"]
                                [rc/button
                                 :label "Cancel"
                                 :class "btn-secondary"
                                 :on-click #(dispatch [:measurement/cancel form-key])]]]
                    [rc/box
                     :align-self :start
                     :child [rc/label
                             :label "add ingredient..."
                             :class "text-muted"
                             :on-click #(dispatch [:form-state/update form-key :show-new-measurement true])]])]])))

(defn editable-measurement-item [form-key]
  (let [mouse-over? (r/atom false)]
    (fn [{:keys [_id ingredient quantity unit] :as measurement} form-key]
      [rc/box
       :style {:padding          "0.3em"
               :background-color (if @mouse-over? "#eee")}
       :attr {:on-mouse-over (handler-fn (reset! mouse-over? true))
              :on-mouse-out  (handler-fn (reset! mouse-over? false))}
       :child (compose-measurement measurement true mouse-over? form-key)])))

(defn measurements [form-key]
  (let [measurements (subscribe [:form-state form-key :measurements])]
    (fn []
      [rc/v-box
       :gap "1em"
       :children [[rc/box
                   :child [rc/title :level :level3 :label "Ingredients"]]
                  [rc/v-box
                   :children [(for [[idx measurement] (indexed @measurements)]
                                ^{:key idx} [editable-measurement-item measurement form-key])]]
                  [new-measurement form-key]]])))

(defn recipe-form [form-key]
  [rc/v-box
   :children [[rc/title :level :level1 :underline? true :label "New Recipe"]
              [rc/h-box
               :gap "2em"
               :children [[rc/v-box
                           :gap "1em"
                           :children [[wired-textbox {:label "Name"
                                                      :form  form-key
                                                      :key   :name}]
                                      [wired-textbox {:label    "Description"
                                                      :form     form-key
                                                      :key      :description
                                                      :textarea true}]
                                      [wired-textbox {:label "Portions"
                                                      :form  form-key
                                                      :key   :portions}]
                                      [wired-textbox {:label    "Preparation"
                                                      :form     form-key
                                                      :key      :preparation
                                                      :textarea true}]]]
                          [rc/border
                           :l-border "1px solid lightgrey"
                           :child [rc/v-box
                                   :style {:padding-left "1em"}
                                   :children [[measurements form-key]]]]]]
              [rc/gap :size "1em"]
              [rc/line]
              [rc/gap :size "0.5em"]
              [rc/h-box
               :align :center
               :gap "0.5em"
               :children [[rc/button
                           :label "Save"
                           :class "btn-primary"
                           :on-click #(dispatch [:recipe/save form-key])]
                          [rc/md-circle-icon-button
                           :md-icon-name "zmdi-delete"
                           :tooltip "Clear form"
                           :on-click #(dispatch [:recipe/clear form-key])]]]]])

(defn new-recipe []
  (let [form-key :new-recipe]
    [recipe-form form-key]))

;; recipe edit ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn edit-recipe []
  (let [form-key :edit-recipe]
    [recipe-form form-key]))


;; recipe list ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn recipe-list-item []
  (let [mouse-over? (r/atom false)
        selected-recipe (subscribe [:state/selected-recipe])]
    (fn [{:keys [_id name description portions preparation categories measurements]}]
      [rc/border
       :r-border "1px solid lightgrey"
       :child [rc/box
               :style {:padding          "1em"
                       :background-color (if (or (= _id (:_id @selected-recipe))
                                                 @mouse-over?)
                                           "#eee")}
               :attr {:on-click      #(dispatch [:recipe/select _id])
                      :on-mouse-over (handler-fn (reset! mouse-over? true))
                      :on-mouse-out  (handler-fn (reset! mouse-over? false))}
               :child [rc/label :label name]]])))

(defn recipe-list [recipes]
  [rc/v-box
   :children (for [recipe @recipes]
               ^{:key (:_id recipe)} [recipe-list-item recipe])])

(defn recipe-list-menu []
  (let [recipes (subscribe [:data/recipes])]
    (fn []
      [rc/v-box
       :children [[rc/h-box
                   :gap "1"
                   :align :center
                   :children [[rc/box
                               :child [rc/title :level :level1 :label "Recipes"]]
                              [rc/md-circle-icon-button
                               :style {:margin-top "1em"}
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

(defn db-state []
  (let [db (subscribe [:db])]
    (fn []
      [rc/v-box
       :children [(edn->hiccup @db)]])))


;; app ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def panes {:recipe-details recipe-details
            :new-recipe     new-recipe
            :edit-recipe    edit-recipe
            :db-state       db-state})

(defn copa-app []
  (let [active-pane (subscribe [:state :active-pane])]
    (fn []
      [rc/v-box
       :children [[rc/h-box
                   :justify :around
                   :gap "4em"
                   :children [[rc/v-box
                               :size "2"
                               :children [[recipe-list-menu]
                                          [rc/button
                                           :label "state"
                                           :class "btn-default"
                                           :on-click #(dispatch [:state/update :active-pane :db-state])]]]
                              [rc/v-box
                               :size "8"
                               :children [(if @active-pane
                                            [(@active-pane panes)]
                                            [rc/box
                                             :child [:div]])]]]]]])))