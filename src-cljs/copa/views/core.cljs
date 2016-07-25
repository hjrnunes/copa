(ns copa.views.core
  (:require-macros [copa.macros :refer [handler-fn]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [re-com.core :as rc :refer-macros [handler-fn]]
            [soda-ash.element :as s]
            [reagent-forms.core :refer [bind-fields]]
            [plumbing.core :refer [indexed]]
            [json-html.core :refer [edn->hiccup]]
            [copa.views.recipes :refer [recipes-section]]
            [copa.views.ingredients :refer [ingredients-section]]
            [copa.views.util :refer [wired-textbox]]))

(defn db-state []
  (let [db (subscribe [:db])]
    (fn []
      [rc/v-box
       :children [(edn->hiccup @db)]])))

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
      [:input {:field :password :id :password :placeholder "senha"}]]]
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

;; app ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;(defn copa-app []
;  (let [active-main-pane (subscribe [:state :active-main-pane])
;        force-login (subscribe [:state :force-login])
;        loading (subscribe [:loading])]
;    (fn []
;      (if-not @force-login
;        [rc/v-box
;         :size "1 0 auto"
;         :style {:margin-left  "1em"
;                 :margin-right "1em"}
;         :children [[rc/h-box
;                     :size "1 0 auto"
;                     :justify :start
;                     :gap "1em"
;                     :children [[rc/v-box
;                                 :justify :between
;                                 :style {:background-color "EBF9D3"}
;                                 :children [[rc/v-box
;                                             :size "none"
;                                             :padding "0.5em"
;                                             :gap "1em"
;                                             :children [[rc/gap
;                                                         :size "0.5em"]
;                                                        [rc/box
;                                                         :child [:img {:src "images/logo.png"}]]
;                                                        [rc/md-circle-icon-button
;                                                         :md-icon-name "zmdi-book"
;                                                         :tooltip "Receitas"
;                                                         :tooltip-position :below-right
;                                                         :on-click #(dispatch [:state/update :active-main-pane :recipes])]
;                                                        [rc/md-circle-icon-button
;                                                         :md-icon-name "zmdi-shopping-cart"
;                                                         :tooltip "Ingredientes"
;                                                         :tooltip-position :below-right
;                                                         :on-click #(dispatch [:state/update :active-main-pane :ingredients])]
;                                                        [rc/md-circle-icon-button
;                                                         :md-icon-name "zmdi-search"
;                                                         :tooltip "State"
;                                                         :tooltip-position :below-right
;                                                         :on-click #(dispatch [:state/update :active-main-pane :db-state])]]]
;                                            (when @loading
;                                              [rc/v-box
;                                               :children [[rc/throbber]]])]]
;                                (if @active-main-pane
;                                  [(@active-main-pane main-panes)]
;                                  [recipes-section])]]]]
;        [login]))))

(defn gen-item-class [id label fn active-pane]
  [:a.item
   (-> {:on-click fn}
       (merge (when (= id @active-pane)
                {:class "active"})))
   label])

(defn copa-menu [active-main-pane]
  [:div.ui.stackable.container.secondary.menu
   [:div.item
    [:img {:src "images/logo.png"}]]
   (gen-item-class :recipes "Receitas" #(dispatch [:state/update :active-main-pane :recipes]) active-main-pane)
   (gen-item-class :ingredients "Ingredientes" #(dispatch [:state/update :active-main-pane :ingredients]) active-main-pane)
   (gen-item-class :db-state "Estado" #(dispatch [:state/update :active-main-pane :db-state]) active-main-pane)])

(def main-panes {:recipes     recipes-section
                 :ingredients ingredients-section
                 :db-state    db-state})

(defn copa-app []
  (let [active-main-pane (subscribe [:state :active-main-pane])
        force-login (subscribe [:state :force-login])
        loading (subscribe [:loading])]
    (fn []
      (if-not @force-login
        [:div
         (copa-menu active-main-pane)
         [:div.ui.container
          (if @active-main-pane
            [(@active-main-pane main-panes)]
            [recipes-section])
          ]]
        [login]))))