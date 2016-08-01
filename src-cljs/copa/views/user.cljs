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

(defn admin-user-details [user]
  [:div.twelve.wide.column
   [:div.row
    [:h3.ui.header
     (:username @user)]]
   [:div.row
    [:div.ui.list
     [:div.item
      [:div.ui.label
       "Admin"
       [:div.detail (str (get @user :admin false))]]]]]])

(defn user-list-item [user]
  [:div.item
   {:on-click #(dispatch [:user/select (:username user)])}
   [:div.content
    [:div.header
     (:username user)]]])

(defn add-user-button []
  (menu-button :i.plus.icon "olive" "Novo utilizador"
               (handler-fn
                 (dispatch [:recipe/select nil])
                 (dispatch [:state/update :active-user-pane :edit-user]))))

(defn edit-user-button []
  (menu-button :i.edit.icon "yellow" "Editar utilizador"
               #(dispatch [:state/update :active-user-pane :edit-user])))

(defn delete-user-button []
  (menu-button :i.trash.icon "red" "Apagar utilizador"
               #(dispatch [:state/update :active-user-pane :edit-recipe])))

(defn users-details []
  (let [users (subscribe [:sorted/users])
        selected-user (subscribe [:state/selected-user])]
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
          [delete-user-button])
        [:div.right.menu
         [:div.ui.icon.item
          {:on-click (handler-fn (dispatch [:get/users]))}
          [:i.refresh.icon]]]]
       [:div.ui.bottom.attached.segment
        [:div.ui.two.column.relaxed.divided.grid
         [:div.four.wide.column
          [:div.ui.selection.list
           (for [[idx user] (indexed @users)]
             ^{:key idx} [user-list-item user])]]
         (when @selected-user
           [admin-user-details selected-user])]]])))


(defn user-section []
  (let [user (subscribe [:state :user])]
    (fn []
      [:div.sixteen.wide.column
       [user-details]
       (when (:admin @user)
         [users-details])])))

