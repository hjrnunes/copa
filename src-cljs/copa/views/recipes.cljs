(ns copa.views.recipes
  (:require-macros [copa.macros :refer [handler-fn]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [re-com.core :as rc]
            [reagent-forms.core :refer [bind-fields]]
            [clojure.string :refer [join capitalize lower-case]]
            [markdown.core :refer [md->html]]
            [plumbing.core :refer [indexed]]
            [json-html.core :refer [edn->hiccup]]
            [copa.views.util :refer [wired-textbox]]
            [copa.util :refer [vec-remove]]))

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

;; recipe creation ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

(defn add-form-measurement [form]
  (when (get @form :measurement)
    (swap! form assoc :measurements (conj (get @form :measurements []) (get @form :measurement)))
    (swap! form dissoc :measurement)))

(defn remove-form-measurement [form key]
  (swap! form assoc :measurements (vec-remove (get @form :measurements []) key)))

(defn recipe-form-measurent-item []
  (let [mouse-over? (r/atom false)]
    (fn [form key {:keys [_id ingredient quantity unit]}]
      [:div.item
       {:on-mouse-over (handler-fn (reset! mouse-over? true))
        :on-mouse-out  (handler-fn (reset! mouse-over? false))}
       (when @mouse-over?
         [:div.right.floated.content
          [:div.ui.basic.red.tiny.label
           {:on-click (handler-fn (remove-form-measurement form key)
                                  (.preventDefault event))}
           "X"]])
       (when quantity
         [:div.right.floated.content
          [:div.ui.basic.tiny.label
           quantity
           (when unit
             [:div.detail (lower-case unit)])]])
       (when ingredient
         [:div.middle.aligned.content
          (capitalize ingredient)])])))

(defn new-recipe []
  (let [form (r/atom {})]
    (fn []
      (println @form)
      [:div.ui.two.column.relaxed.divided.grid
       [:div.row
        [:div.sixteen.wide.column
         [:div.ui.breadcrumb
          [:a.section
           {:on-click #(dispatch [:state/update :active-recipe-pane :recipe-list])}
           "Receitas"]
          [:i.right.angle.icon.divider]
          [:div.active.section
           "Nova receita"]]]]
       [:div.row
        [:div.sixteen.wide.column
         [:form.ui.large.form
          [bind-fields [:input {:field :text :id :title :placeholder "Título..."}] form]]]]
       [:div.row
        [:div.twelve.wide.column
         [:form.ui.large.form
          [bind-fields [:input {:field :text :id :description :placeholder "Descrição..."}] form]]]
        [:div.four.wide.column
         [:form.ui.large.form
          [bind-fields [:input {:field :text :id :portions :placeholder "Porções..."}] form]]]]
       [:div.ui.divider]
       [:div.row
        [:div.twelve.wide.column
         [:form.ui.large.form
          [bind-fields [:input {:field :text_area :id :preparation :placeholder "Preparação..."}] form]]]
        [:div.four.wide.column
         [:h5.ui.header "Ingredientes"]
         [:div.row
          [:div.twelve.wide.column]
          [:div.four.wide.column
           [:div.ui.tiny.divided.list
            (for [[idx measurement] (indexed (:measurements @form))]
              ^{:key idx} [recipe-form-measurent-item form idx measurement])]
           [:div.ui.form
            [:div.inline.fields
             [:div.four.wide.field
              [bind-fields [:input {:field :text :id :measurement.quantity :placeholder "q"}] form]]
             [:div.four.wide.field
              [bind-fields [:input {:field :text :id :measurement.unit :placeholder "u"}] form]]
             [:div.six.wide.field
              [bind-fields [:input {:field :text :id :measurement.ingredient :placeholder "i"}] form]]
             [:button.ui.green.icon.basic.button
              {:type     "button"
               :on-click (handler-fn (add-form-measurement form))}
              [:i.checkmark.icon]]]]]]]]
       [:div.row
        [:div.twelve.wide.column
         [:div.ui.right.floated.buttons
          [:button.ui.button
           {:on-click (handler-fn (reset! form {})
                                  (dispatch [:state/update :active-recipe-pane :recipe-list]))}
           "Apagar"]
          [:div.or
           {:data-text "ou"}]
          [:button.ui.positive.button
           {:type     "button"
            :on-click #(dispatch [:recipe/save form])}
           "Guardar"]]]
        [:div.four.wide.column]]])))

(defn measurement-item []
  (let []
    (fn [{:keys [_id ingredient quantity unit] :as measurement}]
      [:div.item
       (when quantity
         [:div.right.floated.content
          [:div.ui.basic.tiny.label
           quantity
           (when unit
             [:div.detail (lower-case unit)])]])
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
       [:div {:dangerouslySetInnerHTML {:__html (md->html preparation)}}]]
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

;(defn mycheckbox []
;  (let [doc (r/atom {:name false})]
;    (fn []
;      [:div
;       [:p (str @doc)]
;       [bind-fields
;        [:input {:field :checkbox :id :name}]
;        doc]
;       [:button
;        {:on-click #(reset! doc nil)} "clear"]])))
;
;
;(defn mycheckbox2 []
;  (let [doc (r/atom {:name true})]
;    (fn []
;      [:div
;       [:p (str @doc)]
;       [:input {:type      "checkbox"
;                :field     :checkbox
;                :id        :name
;                :on-change #(swap! doc assoc :name (not (:name @doc)))}]
;       [:button
;        {:on-click #(reset! doc nil)} "clear"]])))
;
;(defn myradio []
;  (let [doc (r/atom {:radioselection true})]
;    (fn []
;      [:div
;       [:p (str @doc)]
;       [bind-fields
;        [:form
;         [:input {:field :radio :value :a :name :radioselection} "foo"]
;         [:input {:field :radio :value :b :name :radioselection} "bar"]]
;        doc]
;       [:button
;        {:on-click #(reset! doc nil)} "clear"]])))

(defn recipe-list []
  (let [recipes (subscribe [:data :recipes])]
    (fn []
      [:div
       ;[mycheckbox]
       ;[myradio]
       ;[mycheckbox2]
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
