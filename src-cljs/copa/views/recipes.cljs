(ns copa.views.recipes
  (:require-macros [copa.macros :refer [handler-fn]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [re-com.core :as rc]
            [reagent-forms.core :refer [bind-fields]]
            [clojure.string :refer [join capitalize]]
            [markdown.core :refer [md->html]]
            [plumbing.core :refer [indexed]]
            [json-html.core :refer [edn->hiccup]]
            [copa.views.util :refer [wired-textbox]]))

;; recipe details ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn compose-measurement [{:keys [ingredient quantity unit] :as measurement} edit? mouse-over? form-key]
  [rc/h-box
   :align :center
   :children [[rc/box
               :size "2"
               :child [rc/label :label (capitalize ingredient)]]
              [rc/h-box
               :size "1"
               :gap "0.2em"
               :children [(when quantity
                            [rc/label :label quantity :class "label label-info"])
                          (when unit
                            [rc/label :label (capitalize unit) :class "label label-warning"])
                          (when (and edit? @mouse-over?)
                            [rc/h-box
                             :children [[rc/gap :size "0.5em"]
                                        [rc/md-icon-button
                                         :md-icon-name "zmdi-delete"
                                         :tooltip "Apagar"
                                         :size :smaller
                                         :on-click #(dispatch [:measurement/remove form-key measurement])]]])]]]])

(defn measurement-item-old []
  (let [mouse-over? (r/atom false)]
    (fn [{:keys [_id ingredient quantity unit] :as measurement}]
      [rc/box
       :style {:padding          "0.3em"
               :background-color (if @mouse-over? "#EBF9D3")}
       :attr {:on-mouse-over (handler-fn (reset! mouse-over? true))
              :on-mouse-out  (handler-fn (reset! mouse-over? false))}
       :child (compose-measurement measurement false mouse-over? nil)])))

;(defn recipe-measurements-list [recipe]
;  [rc/v-box
;   :gap "1em"
;   :size "1"
;   :children [[rc/title :level :level3 :label "Ingredientes"]
;              [rc/border
;               :l-border "1px solid lightgrey"
;               :child [rc/v-box
;                       :style {:margin-left "1em"}
;                       :children [(for [[idx measurement] (indexed (:measurements @recipe))]
;                                    ^{:key idx} [measurement-item measurement false])]]]]])

(defn recipe-preparation []
  (fn [{:keys [name description portions preparation categories]}]
    [rc/v-box
     :size "2"
     :gap "1em"
     :children [[rc/title :level :level3 :label "Preparação"]
                [rc/box :child [:div {:dangerouslySetInnerHTML {:__html (md->html preparation)}}]]]]))

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
                             :tooltip "Alterar receita"
                             :on-click (handler-fn
                                         (dispatch [:form-state/load :edit-recipe recipe])
                                         (dispatch [:state/update :active-recipe-pane :edit-recipe]))]]]]]))

(defn recipe-details-subheader []
  (fn [{:keys [name description portions preparation categories] :as recipe}]
    [rc/h-box
     :gap "2em"
     :children [[rc/box
                 :size "2"
                 :child [rc/title :level :level4 :label description]]
                [rc/box
                 :size "1"
                 :child [rc/title :level :level4 :label (when portions
                                                          (join " " [portions "porções"]))]]]]))

;(defn recipe-details-old []
;  (let [recipe (subscribe [:state/selected-recipe])]
;    (fn []
;      (when @recipe
;        [rc/v-box
;         :children [[recipe-details-header @recipe]
;                    [recipe-details-subheader @recipe]
;                    [rc/gap :size "1em"]
;                    [rc/h-box
;                     :gap "2em"
;                     :children [[recipe-preparation @recipe]
;                                [recipe-measurements-list recipe]]]]]))))


;; recipe creation ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn new-measurement [form-key]
  (let [show (subscribe [:form-state form-key :show-new-measurement])]
    (fn []
      [rc/v-box
       :children [(if @show
                    [rc/h-box
                     :align :end
                     :children [[wired-textbox {:label "Quantidade"
                                                :form  form-key
                                                :key   :tmp.measurement/quantity
                                                :width "60px"}]
                                [rc/gap :size "0.5em"]
                                [wired-textbox {:label "Unidade"
                                                :form  form-key
                                                :key   :tmp.measurement/unit
                                                :width "50px"}]
                                [rc/gap :size "1em"]
                                [wired-textbox {:label "Ingrediente"
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
                                 :label "Cancelar"
                                 :class "btn-secondary"
                                 :on-click #(dispatch [:measurement/cancel form-key])]]]
                    [rc/box
                     :align-self :start
                     :child [rc/label
                             :label "adicionar ingrediente..."
                             :class "text-muted"
                             :on-click #(dispatch [:form-state/update form-key :show-new-measurement true])]])]])))

(defn editable-measurement-item [form-key]
  (let [mouse-over? (r/atom false)]
    (fn [{:keys [_id ingredient quantity unit] :as measurement} form-key]
      [rc/box
       :style {:padding          "0.3em"
               :background-color (if @mouse-over? "#EBF9D3")}
       :attr {:on-mouse-over (handler-fn (reset! mouse-over? true))
              :on-mouse-out  (handler-fn (reset! mouse-over? false))}
       :child (compose-measurement measurement true mouse-over? form-key)])))

(defn measurements [form-key]
  (let [measurements (subscribe [:form-state form-key :measurements])]
    (fn []
      [rc/v-box
       :gap "1em"
       :children [[rc/box
                   :child [rc/title :level :level3 :label "Ingredientes"]]
                  [rc/v-box
                   :children [(for [[idx measurement] (indexed @measurements)]
                                ^{:key idx} [editable-measurement-item measurement form-key])]]
                  [new-measurement form-key]]])))

(defn recipe-form-old [form-key]
  [rc/v-box
   :children [[rc/title :level :level1 :underline? true :label "Nova Receita"]
              [rc/h-box
               :gap "2em"
               :children [[rc/v-box
                           :gap "1em"
                           :children [[wired-textbox {:label "Nome"
                                                      :form  form-key
                                                      :key   :name}]
                                      [wired-textbox {:label    "Descrição"
                                                      :form     form-key
                                                      :key      :description
                                                      :textarea true}]
                                      [wired-textbox {:label "Porções"
                                                      :form  form-key
                                                      :key   :portions}]
                                      [wired-textbox {:label    "Preparação"
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
                           :label "Guardar"
                           :class "btn-primary"
                           :on-click #(dispatch [:recipe/save form-key])]
                          [rc/md-circle-icon-button
                           :md-icon-name "zmdi-delete"
                           :tooltip "Limpar ficha"
                           :on-click #(dispatch [:recipe/clear form-key])]]]]])

;(defn new-recipe-old []
;  (let [form-key :new-recipe]
;    [recipe-form form-key]))

;; recipe edit ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;(defn edit-recipe []
;  (let [form-key :edit-recipe]
;    [recipe-form form-key]))


;; recipe list ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;(defn recipe-list-item []
;  (let [mouse-over? (r/atom false)
;        selected-recipe (subscribe [:state/selected-recipe])]
;    (fn [{:keys [_id name description portions preparation categories measurements]}]
;      [rc/border
;       :r-border "1px solid lightgrey"
;       :child [rc/box
;               :style {:padding          "1em"
;                       :background-color (if (or (= _id (:_id @selected-recipe))
;                                                 @mouse-over?)
;                                           "#EBF9D3")}
;               :attr {:on-click      #(dispatch [:recipe/select name])
;                      :on-mouse-over (handler-fn (reset! mouse-over? true))
;                      :on-mouse-out  (handler-fn (reset! mouse-over? false))}
;               :child [rc/label :label name]]])))

;(defn recipe-list [recipes]
;  [rc/v-box
;   :children (for [recipe @recipes]
;               ^{:key (:_id recipe)} [recipe-list-item recipe])])

;(defn recipe-list-menu []
;  (let [recipes (subscribe [:data :recipes])]
;    (fn []
;      [rc/v-box
;       :children [[rc/h-box
;                   :gap "1"
;                   :align :center
;                   :children [[rc/box
;                               :child [rc/title :level :level1 :label "Receitas"]]
;                              [rc/md-circle-icon-button
;                               :style {:margin-top "1em"}
;                               :md-icon-name "zmdi-plus"
;                               :tooltip "Adicionar nova receita"
;                               :on-click #(dispatch [:state/update :active-recipe-pane :new-recipe])]]]
;                  (when-not (empty? @recipes)
;                    [recipe-list recipes])]])))

(defn add-form-measurement [form]
  (when (get @form :measurement)
    (swap! form assoc :measurements (conj (get @form :measurements []) (get @form :measurement)))
    (swap! form dissoc :measurement)))

(defn recipe-form [form]
  [:form.ui.large.form
   [:div.ui.two.column.relaxed.divided.grid
    [:div.row
     [:div.sixteen.wide.column
      [:h5.ui.header "Nova receita"]]]
    [:div.row
     [:div.sixteen.wide.column
      [:input {:field :text :id :title :placeholder "Título..."}]]]
    [:div.row
     [:div.twelve.wide.column
      [:input {:field :text :id :description :placeholder "Descrição..."}]]
     [:div.four.wide.column
      [:input {:field :text :id :portions :placeholder "Porções..."}]]]
    [:div.ui.divider]
    [:div.row
     [:div.twelve.wide.column
      [:input {:field :text_area :id :preparation :placeholder "Preparação..."}]]
     [:div.four.wide.column
      [:h5.ui.header "Ingredientes"]
      [:div.row
       [:div.twelve.wide.column]
       [:div.four.wide.column
        [:div.ui.tiny.divided.list
         (for [[idx {:keys [_id ingredient quantity unit] :as measurement}] (indexed (:measurements @form))]
           ^{:key idx} [:div.item
                        (when quantity
                          [:div.right.floated.content
                           [:div.ui.basic.tiny.label
                            quantity
                            (when unit
                              [:div.detail (capitalize unit)])]])
                        (when ingredient
                          [:div.middle.aligned.content
                           (capitalize ingredient)])])]
        [:div.fields
         [:div.field
          [:input {:field :text :id :measurement.quantity :placeholder "quant"}]]
         [:div.field
          [:input {:field :text :id :measurement.unit :placeholder "unit"}]]
         [:div.field
          [:input {:field :text :id :measurement.ingredient :placeholder "Ing"}]]
         [:button.ui.icon.button
          {:on-click (handler-fn (add-form-measurement form)
                                 (.preventDefault event))}
          [:i.checkmark.icon]]]
        ]]]]]])

(defn new-recipe []
  (let [form (r/atom {})]
    (fn []
      (println @form)
      [bind-fields (recipe-form form) form]
      )))

(defn measurement-item []
  (let []
    (fn [{:keys [_id ingredient quantity unit] :as measurement}]
      [:div.item
       (when quantity
         [:div.right.floated.content
          [:div.ui.basic.tiny.label
           quantity
           (when unit
             [:div.detail (capitalize unit)])]])
       [:div.middle.aligned.content
        (capitalize ingredient)]])))

(defn recipe-details []
  (fn [{:keys [_id name description portions preparation categories measurements]}]
    [:div.ui.two.column.relaxed.divided.grid
     [:div.row
      [:div.twelve.wide.column
       [:i description]]
      [:div.four.wide.column
       (when portions
         [:i (join " " [portions "porções"])])]]
     [:div.ui.divider]
     [:div.row
      [:div.twelve.wide.column
       [:h5.ui.header "Preparação"]]
      [:div.four.wide.column
       [:h5.ui.header "Ingredientes"]]]
     [:div.row
      [:div.twelve.wide.column
       preparation]
      [:div.four.wide.column
       [:div.ui.tiny.divided.list
        (for [[idx measurement] (indexed measurements)]
          ^{:key idx} [measurement-item measurement])
        ]]]]))

(defn recipe-item []
  (let [selected-recipe (subscribe [:state/selected-recipe])
        selected? #(= % (:_id @selected-recipe))]
    (fn [{:keys [_id name description portions preparation categories measurements] :as recipe}]
      [:div
       [:div.title
        (-> {:on-click #(if (selected? _id)
                         (dispatch [:recipe/select nil])
                         (dispatch [:recipe/select name]))}
            (merge (when (selected? _id)
                     {:class "active"})))
        ;[:i.dropdown.icon]
        name]
       [:div.content
        (-> {}
            (merge (when (selected? _id)
                     {:class "active"})))
        [recipe-details recipe]]])))

(defn add-recipe-button []
  (let [add-mouse-over? (r/atom false)]
    (fn []
      [:div.ui.icon.item
       {:on-mouse-over (handler-fn (reset! add-mouse-over? true))
        :on-mouse-out  (handler-fn (reset! add-mouse-over? false))
        :on-click      #(dispatch [:state/update :active-recipe-pane :new-recipe])}
       [:i.plus.icon
        (-> (merge (when-not @add-mouse-over?
                     {:class "disabled"})))]])))

(defn recipe-search []
  [:div.ui.right.align.category.search.item
   [:div.ui.transparent.icon.input
    [:input.prompt {:placeholder "Procurar receita..."}]
    [:div.search.link.icon]]])

(defn recipe-list []
  (let [recipes (subscribe [:data :recipes])]
    (fn []
      [:div
       [:div.ui.top.attached.menu
        [add-recipe-button]
        [:div.right.menu
         [recipe-search]]]
       [:div.ui.bottom.attached.styled.fluid.accordion
        (for [recipe @recipes]
          ^{:key (:_id recipe)} [recipe-item recipe])]])))

(def recipe-panes {:recipe-list recipe-list
                   :new-recipe  new-recipe
                   ;:edit-recipe edit-recipe
                   })

(defn recipes-section []
  (let [active-recipe-pane (subscribe [:state :active-recipe-pane])]
    (fn []
      (if @active-recipe-pane
        [(@active-recipe-pane recipe-panes)]
        [recipe-list]))))

;(defn recipes-section []
;  (let [active-recipe-pane (subscribe [:state :active-recipe-pane])]
;    (fn []
;      [rc/h-box
;       :size "1"
;       :justify :around
;       :gap "4em"
;       :children [[rc/v-box
;                   :size "2"
;                   :children [[recipe-list-menu]]]
;                  [rc/v-box
;                   :size "8"
;                   :children [(if @active-recipe-pane
;                                [(@active-recipe-pane recipe-panes)]
;                                [rc/box
;                                 :child [:div]])]]]])))
