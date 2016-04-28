(ns copa.views.core
  (:require-macros [copa.macros :refer [handler-fn]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [re-com.core :as rc :refer-macros [handler-fn]]
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

(defn login []
  (let [form-key :login
        alert (subscribe [:state :alert])]
    (fn []
      [rc/v-box
       :children [[rc/v-box
                   :justify :center
                   :align :center
                   :size "1 0 auto"
                   :children [(when @alert
                                [rc/alert-box
                                 :alert-type :danger
                                 :body "Sessão expirada!"
                                 :closeable? true
                                 :on-close #(dispatch [:state/update :alert false])])
                              [rc/gap
                               :size "1em"]
                              [rc/title
                               :level :level2
                               :label "COPA - identificação"]
                              [rc/gap
                               :size "1em"]
                              [wired-textbox {:placeholder "utilizador"
                                              :form        form-key
                                              :key         :username}]
                              [rc/gap
                               :size "0.5em"]
                              [wired-textbox {:placeholder "senha"
                                              :form        form-key
                                              :key         :password
                                              :password    true}]
                              [rc/gap
                               :size "1em"]
                              [rc/button
                               :label "Entrar"
                               :class "btn-primary"
                               :on-click #(dispatch [:data/login form-key])]]]]])))

;; app ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def main-panes {:recipes     recipes-section
                 :ingredients ingredients-section
                 :db-state    db-state})

(defn copa-app []
  (let [active-main-pane (subscribe [:state :active-main-pane])
        force-login (subscribe [:state :force-login])
        loading (subscribe [:loading])]
    (fn []
      (if-not @force-login
        [rc/v-box
         :size "1 0 auto"
         :style {:margin-left  "1em"
                 :margin-right "1em"}
         :children [[rc/h-box
                     :size "1 0 auto"
                     :justify :start
                     :gap "1em"
                     :children [[rc/v-box
                                 :justify :between
                                 :style {:background-color "EBF9D3"}
                                 :children [[rc/v-box
                                             :size "none"
                                             :padding "0.5em"
                                             :gap "1em"
                                             :children [[rc/gap
                                                         :size "0.5em"]
                                                        [rc/box
                                                         :child [:img {:src "images/logo.png"}]]
                                                        [rc/md-circle-icon-button
                                                         :md-icon-name "zmdi-book"
                                                         :tooltip "Receitas"
                                                         :tooltip-position :below-right
                                                         :on-click #(dispatch [:state/update :active-main-pane :recipes])]
                                                        [rc/md-circle-icon-button
                                                         :md-icon-name "zmdi-shopping-cart"
                                                         :tooltip "Ingredientes"
                                                         :tooltip-position :below-right
                                                         :on-click #(dispatch [:state/update :active-main-pane :ingredients])]
                                                        [rc/md-circle-icon-button
                                                         :md-icon-name "zmdi-search"
                                                         :tooltip "State"
                                                         :tooltip-position :below-right
                                                         :on-click #(dispatch [:state/update :active-main-pane :db-state])]]]
                                            (when @loading
                                              [rc/v-box
                                               :children [[rc/throbber]]])]]
                                (if @active-main-pane
                                  [(@active-main-pane main-panes)]
                                  [recipes-section])]]]]
        [login]))))
