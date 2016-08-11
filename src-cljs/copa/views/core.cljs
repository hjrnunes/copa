(ns copa.views.core
  (:require-macros [copa.macros :refer [handler-fn]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent-forms.core :refer [bind-fields]]
            [plumbing.core :refer [indexed]]
            [json-html.core :refer [edn->hiccup]]
            [copa.views.recipes :refer [recipes-section]]
            [copa.views.ingredients :refer [ingredients-section]]
            [copa.views.user :refer [user-section]]
            [copa.routes :refer [url-for]]))

(defn db-state []
  (let [db (subscribe [:db])]
    (fn []
      (edn->hiccup @db))))

(defn loading-status [loading]
  [:div
   (if @loading
     [:span.label.label-warning "Syncing"]
     [:span.label.label-info "Synced"])])

(defn login-form [form]
  [:form.ui.large.form
   [:div.ui.stacked.segment
    [:div.field
     [:div.ui.left.icon.input
      [:i.user.icon]
      [:input {:field :text :id :username :placeholder "utilizador"}]]]
    [:div.field
     [:div.ui.left.icon.input
      [:i.lock.icon]
      [:input {:field        :password
               :id           :password
               :placeholder  "senha"
               :on-key-press (handler-fn (if (= 13 (.-charCode event))
                                           (dispatch [:data/login @form])))}]]]
    [:div.ui.fluid.large.olive.submit.button
     {:on-click #(dispatch [:data/login @form])}
     "Entrar"]]])

(defn login []
  (let [form (r/atom {})
        alert (subscribe [:state :alert])]
    (fn []
      [:div.ui.middle.aligned.center.aligned.grid
       {:style {:height "100%"}}
       [:div.column
        {:style {:max-width "450px"}}
        [:h2.ui.olive.header
         [:div.content
          "COPA - identificação"]]
        [bind-fields (login-form form) form]
        (println "Alert" @alert)
        (when @alert
          [:div.ui.error.message "Sessão expirada!"])]])))

(defn gen-item-class [id label target active-pane]
  [:a.item
   (-> {:href target}
       (merge (when (= id @active-pane)
                {:class "active"})))
   label])

(defn copa-menu []
  (let [user (subscribe [:state :user])]
    (fn [active-main-pane]
      [:div.ui.stackable.container.secondary.menu
       [:div.item
        [:img {:src "images/logo.png"}]]
       (gen-item-class :recipes "Receitas" (url-for :recipes) active-main-pane)
       (gen-item-class :ingredients "Ingredientes" (url-for :ingredients) active-main-pane)
       [:div.right.menu
        [:a.item
         (-> {:href (url-for :user)}
             (merge (when (= :user @active-main-pane)
                      {:class "active"})))
         [:i.user.icon]
         (:username @user)]]])))

(def main-panes {:home        recipes-section
                 :recipes     recipes-section
                 :ingredients ingredients-section
                 :user        user-section
                 :db-state    db-state})

(defn copa-app []
  (let [active-main-pane (subscribe [:state :active-main-pane])
        force-login (subscribe [:state :force-login])
        loading (subscribe [:loading])]
    (fn []
      (println "AP" @active-main-pane)
      (if-not @force-login
        [:div
         [copa-menu active-main-pane]
         [:div.ui.container
          (if @active-main-pane
            [(@active-main-pane main-panes)]
            [recipes-section])
          ]]
        [login]))))