(ns copa.views.recipes
  (:require-macros [copa.macros :refer [handler-fn]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [goog.dom :as dom]
            [reagent-forms.core :refer [bind-fields]]
            [clojure.string :refer [join lower-case]]
            [markdown.core :refer [md->html]]
            [plumbing.core :refer [indexed]]
            [json-html.core :refer [edn->hiccup]]
            [copa.views.util :refer [menu-button]]
            [copa.util :refer [vec-remove t capitalize]]
            [copa.routes :refer [push-url-for]]))

(defn add-form-measurement [form]
  (when (get @form :measurement)
    (swap! form assoc :measurements (conj (get @form :measurements []) (get @form :measurement)))
    (swap! form dissoc :measurement)))

(defn remove-form-measurement [form key]
  (swap! form assoc :measurements (vec-remove (get @form :measurements []) key)))

(defn recipe-form-measurent-item []
  (let [mouse-over? (r/atom false)]
    (fn [form key {:keys [recipe_id ingredient quantity unit]}]
      [:div.item
       {:on-mouse-over (handler-fn (reset! mouse-over? true))
        :on-mouse-out  (handler-fn (reset! mouse-over? false))}
       (when @mouse-over?
         [:div.right.floated.content
          [:div.ui.basic.red.tiny.label
           {:on-click (handler-fn (remove-form-measurement form key))}
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

(defn md-tooltip [lang]
  [:span
   [:span
    {:data-tooltip  (t @lang :recipes/edit-md-tt)
     :data-position "top left"}
    [:i.orange.idea.link.icon]]
   [:a {:href "https://guides.github.com/features/mastering-markdown"}
    (t @lang :recipes/edit-md-link)]])

(defn edit-recipe []
  (let [selected-recipe (subscribe [:selected-recipe])
        form (r/atom (or @selected-recipe {}))
        lang (subscribe [:lang])]
    (fn []
      [:div.ui.two.column.relaxed.divided.grid
       [:div.row
        [:div.sixteen.wide.column
         [:div.ui.breadcrumb
          [:a.section
           {:on-click #(dispatch [:push-url-for :recipes])}
           (t @lang :recipes/breadcrumb-recipes)]
          [:i.right.angle.icon.divider]
          [:div.active.section
           (t @lang :recipes/breadcrumb-new-recipe)]]]]
       [:div.row
        [:div.sixteen.wide.column
         [:form.ui.large.form
          [bind-fields [:input {:field :text :id :name :placeholder (str (t @lang :recipes/details-name) "...")}] form]]]]
       [:div.row
        [:div.twelve.wide.column
         [:form.ui.large.form
          [bind-fields [:input {:field :text :id :description :placeholder (str (t @lang :recipes/details-description) "...")}] form]]]
        [:div.four.wide.column
         [:form.ui.large.form
          [bind-fields [:input {:field :text :id :portions :placeholder (str (capitalize (t @lang :recipes/details-portions-p)) "...")}] form]]]]
       [:div.row
        [:div.twelve.wide.column
         [:form.ui.large.form
          [bind-fields [:input {:field :text :id :source :placeholder (str (t @lang :recipes/details-source) "...")}] form]]]
        [:div.four.wide.column
         [:form.ui.large.form
          [bind-fields [:input {:field :text :id :duration :placeholder (str (t @lang :recipes/details-duration) "...")}] form]]]]
       [:div.ui.divider]
       [:div.row
        [:div.twelve.wide.column
         [:form.ui.large.form
          [:div.field
           [bind-fields [:input {:field :text_area :id :preparation :placeholder (str (t @lang :recipes/details-preparation) "...")}] form]]
          [md-tooltip lang]]]
        [:div.four.wide.column
         [:h5.ui.header (t @lang :recipes/details-ingredients)]
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
              [bind-fields [:input {:field        :text
                                    :id           :measurement.ingredient
                                    :placeholder  "i"
                                    :on-key-press (handler-fn (if (= 13 (.-charCode event))
                                                                (add-form-measurement form)))}] form]]
             [:button.ui.green.icon.basic.button
              {:type     "button"
               :on-click (handler-fn (add-form-measurement form))}
              [:i.checkmark.icon]]]]]]]]
       [:div.row
        [:div.twelve.wide.column
         [:div.ui.right.floated.buttons
          [:button.ui.button
           {:type     "button"
            :on-click (handler-fn (reset! form {})
                                  (dispatch [:state/update :active-recipe-pane :recipe-list]))}
           (t @lang :recipes/edit-button-label-cancel)]
          [:div.or
           {:data-text (t @lang :recipes/edit-button-label-or)}]
          [:button.ui.positive.button
           {:type     "button"
            :on-click #(dispatch [:recipe/save @form])}
           (t @lang :recipes/edit-button-label-save)]]]
        [:div.four.wide.column]]])))

(defn measurement-item []
  (let []
    (fn [{:keys [ingredient quantity unit] :as measurement}]
      [:div.item
       (when quantity
         [:div.right.floated.content
          [:div.ui.basic.tiny.horizontal.label
           quantity
           (when unit
             [:div.detail (lower-case unit)])]])
       [:div.middle.aligned.content
        (capitalize ingredient)]])))

(defn meta-item [icon content]
  [:span
   {:data-tooltip  content
    :data-inverted ""}
   [icon]])

;; recipe details ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn recipe-details []
  (let [lang (subscribe [:lang])]
    (fn [{:keys [name description source duration portions preparation user categories measurements]}]
      [:div.ui.two.column.relaxed.divided.grid
       [:div.row
        [:div.twelve.wide.column
         [:h4.ui.olive.header
          (capitalize name)
          [:div.sub.header
           [:i description]]]]
        (when (or user source duration portions)
          [:div.four.wide.column
           (when (or user source duration)
             [:div.row
              (when user
                [meta-item :i.user.icon user])
              (when source
                [meta-item :i.book.icon source])
              (when duration
                [meta-item :i.clock.icon duration])])
           (when portions
             [:div.row
              (if (= "1" portions)
                [:i (join " " [portions (t @lang :recipes/details-portions-s)])]
                [:i (join " " [portions (t @lang :recipes/details-portions-p)])])])])]
       [:div.ui.divider]
       [:div.row
        [:div.twelve.wide.column
         [:h5.ui.header (t @lang :recipes/details-preparation)]]
        [:div.four.wide.column
         [:h5.ui.header (t @lang :recipes/details-ingredients)]]]
       [:div.row
        [:div.twelve.wide.column
         [:div {:dangerouslySetInnerHTML {:__html (md->html preparation)}}]]
        [:div.four.wide.column
         [:div.ui.small.divided.list
          (for [[idx measurement] (indexed measurements)]
            ^{:key idx} [measurement-item measurement])]]]])))

(defn recipe-list-item []
  (let [selected-recipe (subscribe [:selected-recipe])
        selected? #(= % (:recipe_id @selected-recipe))]
    (fn [{:keys [recipe_id name description portions preparation categories measurements] :as recipe}]
      [:div.item
       (-> {:on-click #(if (selected? recipe_id)
                        (dispatch [:push-url-for :recipes])
                        (dispatch [:push-url-for :recipe :id recipe_id]))}
           (merge (when (selected? recipe_id)
                    {:class "active"})))
       [:div.content
        (capitalize name)]])))

(defn add-recipe-button [button-label]
  (menu-button :i.plus.icon "olive" button-label
               (handler-fn
                 (dispatch [:push-url-for :recipe-new]))))

(defn edit-recipe-button [button-label selected]
  (menu-button :i.edit.icon "yellow" button-label
               #(dispatch [:push-url-for :recipe-edit :id (:recipe_id @selected)])))

(defn delete-recipe-button [button-label selected]
  (menu-button :i.trash.icon "red" button-label
               (handler-fn
                 (dispatch [:push-url-for :recipes])
                 (dispatch [:recipe/delete (:recipe_id @selected)]))))

(defn recipe-search-dispatch [selected]
  (handler-fn (when selected
                (dispatch [:push-url-for :recipe :id selected]))
              (set! (.-value (dom/getElement "recsearch")) nil)))

(defn recipe-search [ph-label]
  (let [recipes (subscribe [:sorted-recipes])
        selected (r/atom nil)]
    (fn []
      [:div.ui.right.align.search.item
       [:div.ui.transparent.icon.input
        [:input#recsearch.prompt {:placeholder ph-label
                                  :on-change   (handler-fn
                                                 (.. (js/$ ".ui.search")
                                                     (search (clj->js {:source       @recipes
                                                                       :searchFields ["name" "description"]
                                                                       :fields       {"description" "description"
                                                                                      "title"       "name"}
                                                                       :onSelect     (fn [result _]
                                                                                       (reset! selected (:recipe_id (js->clj result :keywordize-keys true))))}))))}]
        [:i.search.link.icon
         {:on-click (recipe-search-dispatch @selected)}]]
       [:div.results]])))

(defn recipe-list []
  (let [recipes (subscribe [:sorted-recipes])
        selected-recipe (subscribe [:selected-recipe])
        lang (subscribe [:lang])]
    (fn []
      [:div
       [:div.ui.top.attached.menu
        [add-recipe-button (t @lang :recipes/menu-add)]
        (when @selected-recipe
          [edit-recipe-button (t @lang :recipes/menu-edit) selected-recipe])
        (when @selected-recipe
          [delete-recipe-button (t @lang :recipes/menu-delete) selected-recipe])
        [:div.right.menu
         [recipe-search (t @lang :recipes/menu-search-ph)]]]
       [:div.ui.bottom.attached.segment
        [:div.ui.two.column.relaxed.divided.grid
         [:div.four.wide.column
          [:div.ui.middle.aligned.selection.list
           (for [recipe @recipes]
             ^{:key (:recipe_id recipe)} [recipe-list-item recipe])]]
         [:div.twelve.wide.column
          (when @selected-recipe
            [recipe-details @selected-recipe])]]]])))

(def recipe-panes {:recipe-list recipe-list
                   :edit-recipe edit-recipe})

(defn recipes-section []
  (let [active-recipe-pane (subscribe [:active-recipe-pane])]
    (fn []
      (if @active-recipe-pane
        [(@active-recipe-pane recipe-panes)]
        [recipe-list]))))