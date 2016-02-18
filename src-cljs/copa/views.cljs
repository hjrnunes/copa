(ns copa.views
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch]]
            [re-com.core :as rc :refer-macros [handler-fn]]
            [clojure.string :refer [join capitalize]]
            [plumbing.core :refer [indexed]]))

;; recipe details ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn compose-measurement [ingredient quantity unit]
  [rc/h-box
   :gap "0.2em"
   :align :center
   :children [[rc/label :label (capitalize ingredient)]
              [rc/gap :size "1em"]
              [rc/label :label quantity :class "label label-info"]
              (when unit
                [rc/label :label (capitalize unit) :class "label label-warning"])]])

(defn measurement-item []
  (let [mouse-over? (atom false)]
    (fn [{:keys [db/id measurement/ingredient measurement/quantity measurement/unit]}]
      [rc/box
       :style {:padding          "0.3em"
               :background-color (if @mouse-over? "#eee")}
       :attr {:on-mouse-over (handler-fn (reset! mouse-over? true))
              :on-mouse-out  (handler-fn (reset! mouse-over? false))}
       :child (compose-measurement (get ingredient :ingredient/name ingredient) quantity unit)])))

(defn recipe-measurements-list [recipe]
  [rc/v-box
   :children [[rc/box
               :style {:padding "1em"}
               :child [rc/title :level :level3 :label "Ingredients"]]
              [rc/border
               :l-border "1px solid lightgrey"
               :child [rc/v-box
                       :style {:margin-left "1em"}
                       :children [(for [measurement (:recipe/measurements @recipe)]
                                    ^{:key (:db/id measurement)} [measurement-item measurement])]]]]])

(defn recipe-preparation []
  (fn [{:keys [recipe/name recipe/description recipe/portions recipe/preparation recipe/categories]}]
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
  (fn [{:keys [recipe/name recipe/description recipe/portions recipe/preparation recipe/categories]}]
    [rc/v-box
     :children [
                [rc/title :level :level1 :underline? true :label name]]]))

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
         :model (or @model "")
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

(defn measurements [form-key]
  (let [measurements (subscribe [:form-state form-key :recipe/measurements])]
    (fn []
      [rc/v-box
       :gap "1em"
       :children [[rc/box
                   :child [rc/title :level :level3 :label "Ingredients"]]
                  [rc/v-box
                   :children [(for [[idx measurement] (indexed @measurements)]
                                ^{:key idx} [measurement-item measurement])]]
                  [new-measurement form-key]]])))

(defn new-recipe []
  (let [form-key :new-recipe]
    [rc/v-box
     :children [[rc/title :level :level1 :underline? true :label "New Recipe"]
                [rc/h-box
                 :gap "2em"
                 :children [[rc/v-box
                             :gap "1em"
                             :children [[wired-textbox {:label "Name"
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
                             :label "Add recipe"
                             :class "btn-primary"
                             :on-click #(dispatch [:recipe/create form-key])]
                            [rc/md-circle-icon-button
                             :md-icon-name "zmdi-delete"
                             :tooltip "Clear form"
                             :on-click #(dispatch [:recipe/clear form-key])]]]]]))


;; recipe list ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn recipe-list-item []
  (let [mouse-over? (atom false)
        selected-recipe (subscribe [:state/selected-recipe])]
    (fn [{:keys [db/id recipe/name recipe/description recipe/portions recipe/preparation recipe/categories recipe/measurements]}]
      [rc/border
       :r-border "1px solid lightgrey"
       :child [rc/box
               :style {:padding          "1em"
                       :background-color (if (or (= id (:db/id @selected-recipe))
                                                 @mouse-over?)
                                           "#eee")}
               :attr {:on-click      #(dispatch [:recipe/select id])
                      :on-mouse-over (handler-fn (reset! mouse-over? true))
                      :on-mouse-out  (handler-fn (reset! mouse-over? false))}
               :child [rc/label :label name]]])))

(defn recipe-list [recipes]
  [rc/v-box
   :children (for [recipe @recipes]
               ^{:key (:db/id recipe)} [recipe-list-item recipe])])

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
                   :justify :around
                   :gap "4em"
                   :children [[rc/v-box
                               :size "2"
                               :children [[recipe-list-menu]]]
                              [rc/v-box
                               :size "8"
                               :children [(if @active-pane
                                            [(@active-pane panes)]
                                            [rc/box
                                             :child [:div]])]]
                              ]]]])))