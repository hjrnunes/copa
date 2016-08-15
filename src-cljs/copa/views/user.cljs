(ns copa.views.user
  (:require-macros [copa.macros :refer [handler-fn]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent-forms.core :refer [bind-fields]]
            [clojure.string :refer [join capitalize lower-case]]
            [markdown.core :refer [md->html]]
            [plumbing.core :refer [indexed]]
            [json-html.core :refer [edn->hiccup]]
            [copa.routes :refer [url-for]]
            [copa.views.util :refer [menu-button]]
            [copa.util :refer [vec-remove t]]))

(defn language-pref [user lang]
  (let [sel-lang (r/atom nil)]
    (r/create-class
      {:reagent-render      (fn []
                              [:row
                               [:div.ui.grid
                                [:div.four.wide.column
                                 [:form.ui.form
                                  [:div.field
                                   [:div.ui.selection.dropdown
                                    [:div.default.text (t lang :user/language)]
                                    [:i.dropdown.icon]
                                    [:div.menu
                                     (for [[idx [txt val flag]] (indexed [["EN" "en" "gb"]
                                                                          ["PT" "pt" "pt"]])]
                                       ^{:key idx} [:div.item {:data-value val}
                                                    [:i
                                                     {:class (str flag " flag")}]
                                                    txt])]]]]]
                                [:div.four.wide.column
                                 (when @sel-lang
                                   [:button.ui.green.icon.basic.button
                                    {:type     "button"
                                     :on-click (handler-fn (dispatch [:user/update-lang (:username user) @sel-lang])
                                                           (reset! sel-lang nil))}
                                    [:i.checkmark.icon]])]]])
       :component-did-mount (fn [comp]
                              (.. (js/$ ".ui.selection.dropdown")
                                  (dropdown (clj->js {:onChange (fn [value, text, item]
                                                                  (reset! sel-lang (keyword value)))}))))}))
  )

(defn user-details []
  (let [user (subscribe [:state :user])
        recipes (subscribe [:user/recipes (:username @user)])
        lang (subscribe [:lang])]
    (fn []
      [:div.row
       [:h3.ui.top.attached.header
        (:username @user)]
       [:div.ui.attached.segment
        (when (get @user :admin false)
          [:a.ui.yellow.right.ribbon.label
           {:href (url-for :db-state)}
           [:i.search.icon]
           (t @lang :admin/state)])
        [:row
         [:div.ui.statistics
          [:div.olive.statistic
           [:div.value
            (count @recipes)]
           [:div.label
            (t @lang :user/recipes)]]
          [:div.olive.statistic
           [:div.value
            (capitalize (name @lang))]
           [:div.label
            (t @lang :user/language)]]]]
        [language-pref @user @lang]]])))

(defn user-form-template [form lang]
  [:div.row
   [:h5.ui.top.attached.header
    (t @lang :admin/new-user-heading)]
   [:div.ui.attached.basic.segment
    [:div.ui.form
     [:div.inline.fields
      [:div.field
       [:label (t @lang :admin/new-user-user-label)]
       [bind-fields [:input {:field :text :id :username :placeholder (t @lang :admin/new-user-user-ph)}] form]]
      [:div.field
       [:label (t @lang :admin/new-user-password-label)]
       [bind-fields [:input {:field :password :id :password :placeholder (t @lang :admin/new-user-password-ph)}] form]]
      [:div.field
       [:div.ui.toggle.checkbox
        [bind-fields [:input {:field :checkbox :id :admin}] form]
        [:label (t @lang :admin/new-user-admin-label)]]]
      [:div.ui.right.floated.buttons
       [:button.ui.button
        {:type     "button"
         :on-click (handler-fn (reset! form {:admin false})
                               (dispatch [:state/update :active-users-pane :user-list]))}
        (t @lang :admin/new-user-button-label-cancel)]
       [:div.or
        {:data-text (t @lang :admin/new-user-button-label-or)}]
       [:button.ui.positive.button
        {:type     "button"
         :on-click (handler-fn (dispatch [:user/save @form])
                               (dispatch [:state/update :active-users-pane :user-list]))}
        (t @lang :admin/new-user-button-label-save)]]]]]])

(defn user-form []
  (let [form (r/atom {:admin false})
        lang (subscribe [:lang])]
    (fn []
      [:div.twelve.wide.column
       [user-form-template form lang]])))

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

(defn add-user-button [button-label]
  (menu-button :i.plus.icon "olive" button-label
               (handler-fn
                 (dispatch [:state/update :active-users-pane :new-user]))))

(defn edit-user-button [button-label]
  (menu-button :i.edit.icon "yellow" button-label
               #(dispatch [:state/update :active-user-pane :edit-user])))

(defn delete-user-button [button-label selected]
  (menu-button :i.trash.icon "red" button-label
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
        active-users-pane (subscribe [:state :active-users-pane])
        lang (subscribe [:lang])]
    (dispatch [:get/users])
    (fn []
      [:div.row
       {:style {:margin-top "1rem"}}
       [:div.ui.top.attached.menu
        [:div.item
         [:h5.ui.header
          "Users"]]
        [add-user-button (t @lang :admin/menu-add)]
        (when @selected-user
          [edit-user-button (t @lang :admin/menu-edit)])
        (when @selected-user
          [delete-user-button (t @lang :admin/menu-delete) selected-user])
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

