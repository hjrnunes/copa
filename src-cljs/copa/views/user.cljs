(ns copa.views.user
  (:require-macros [copa.macros :refer [handler-fn]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent-forms.core :refer [bind-fields]]
            [clojure.string :refer [join capitalize lower-case]]
            [markdown.core :refer [md->html]]
            [plumbing.core :refer [indexed]]
            [json-html.core :refer [edn->hiccup]]
            [copa.views.util :refer [menu-button]]
            [copa.util :refer [vec-remove]]))

(defn user-details []
  (let [user (subscribe [:state :user])
        recipes (subscribe [:user/recipes (:username @user)])]
    (fn []
      [:div.row
       [:h3.ui.top.attached.header
        (:username @user)]
       [:div.ui.attached.segment
        (when (get @user :admin false)
          [:a.ui.yellow.right.ribbon.label
           {:on-click #(dispatch [:state/update :active-main-pane :db-state])}
           [:i.search.icon]
           "Estado"])
        [:div.ui.statistics
         [:div.olive.statistic
          [:div.value
           (count @recipes)]
          [:div.label
           "Receitas"]]]]])))

(defn user-form-template [form]
  [:div.row
   [:h5.ui.top.attached.header
    "Novo utilizador"]
   [:div.ui.attached.basic.segment
    [:div.ui.form
     [:div.inline.fields
      [:div.field
       [:label "Utilizador"]
       [bind-fields [:input {:field :text :id :username :placeholder "utilizador"}] form]]
      [:div.field
       [:label "Senha"]
       [bind-fields [:input {:field :password :id :password :placeholder "senha"}] form]]
      [:div.field
       [:div.ui.toggle.checkbox
        [bind-fields [:input {:field :checkbox :id :admin}] form]
        [:label "Admin"]]]
      [:div.ui.right.floated.buttons
       [:button.ui.button
        {:type     "button"
         :on-click (handler-fn (reset! form {:admin false})
                               (dispatch [:state/update :active-users-pane :user-list]))}
        "Apagar"]
       [:div.or
        {:data-text "ou"}]
       [:button.ui.positive.button
        {:type     "button"
         :on-click (handler-fn (dispatch [:user/save @form])
                               (dispatch [:state/update :active-users-pane :user-list]))}
        "Guardar"]]]]]])

(defn user-form []
  (let [form (r/atom {:admin false})]
    (fn []
      [:div.twelve.wide.column
       [user-form-template form]])))

(defn admin-user-details []
  (let [selected-user (subscribe [:state/selected-user])]
    (fn []
      [:div.twelve.wide.column
       [:div.row
        [:h3.ui.header
         (:username @selected-user)]]
       [:div.row
        [:div.ui.list
         [:div.item
          [:div.ui.label
           "Admin"
           [:div.detail (str (get @selected-user :admin false))]]]]]])))

(defn user-list-item [user]
  [:div.item
   {:on-click (handler-fn
                (dispatch [:user/select (:username user)])
                (dispatch [:state/update :active-users-pane :user-list]))}
   [:div.content
    [:div.header
     (:username user)]]])

(defn add-user-button []
  (menu-button :i.plus.icon "olive" "Novo utilizador"
               (handler-fn
                 (dispatch [:state/update :active-users-pane :new-user]))))

(defn edit-user-button []
  (menu-button :i.edit.icon "yellow" "Editar utilizador"
               #(dispatch [:state/update :active-user-pane :edit-user])))

(defn delete-user-button [selected]
  (menu-button :i.trash.icon "red" "Apagar utilizador"
               #(dispatch [:user/delete (:username @selected)])))

(defn users-list-details []
  (let [users (subscribe [:sorted/users])
        selected-user (subscribe [:state/selected-user])]
    (fn []
      [:div.ui.two.column.relaxed.divided.grid
       [:div.four.wide.column
        [:div.ui.selection.list
         (for [[idx user] (indexed @users)]
           ^{:key idx} [user-list-item user])]]
       (when @selected-user
         [admin-user-details])])))

(def user-panes {:user-list users-list-details
                 :new-user  user-form})

(defn users-admin-panel []
  (let [selected-user (subscribe [:state/selected-user])
        active-users-pane (subscribe [:state :active-users-pane])]
    (dispatch [:get/users])
    (fn []
      [:div.row
       {:style {:margin-top "1rem"}}
       [:div.ui.top.attached.menu
        [:div.item
         [:h5.ui.header
          "Users"]]
        [add-user-button]
        (when @selected-user
          [edit-user-button])
        (when @selected-user
          [delete-user-button selected-user])
        [:div.right.menu
         [:div.ui.icon.item
          {:on-click (handler-fn (dispatch [:get/users]))}
          [:i.refresh.icon]]]]
       [:div.ui.bottom.attached.segment
        (if @active-users-pane
          [(@active-users-pane user-panes)]
          [users-list-details])]])))

(defn user-section []
  (let [user (subscribe [:state :user])]
    (fn []
      [:div.sixteen.wide.column
       [user-details]
       (when (:admin @user)
         [users-admin-panel])])))

