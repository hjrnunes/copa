(ns copa.views.core
  (:require-macros [copa.macros :refer [handler-fn]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [re-com.core :as rc :refer-macros [handler-fn]]
            [plumbing.core :refer [indexed]]
            [json-html.core :refer [edn->hiccup]]
            [copa.views.recipes :refer [recipes-section]]
            [copa.views.ingredients :refer [ingredients-section]]))

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

;; app ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def main-panes {:recipes     recipes-section
                 :ingredients ingredients-section
                 :db-state    db-state})

(defn copa-app []
  (let [active-main-pane (subscribe [:state :active-main-pane])]
    (fn []
      [rc/v-box
       :children [[rc/h-box
                   :size "1 0 auto"
                   :justify :start
                   :gap "1em"
                   :children [[rc/v-box
                               ;:justify :around
                               :size "none"
                               :padding "0.5em"
                               :gap "1em"
                               :style {:background-color "ghostwhite"}
                               :children [[rc/gap
                                           :size "0.5em"]
                                          [rc/box
                                           :child [:img {:src "images/logo.png"}]]
                                          [rc/md-circle-icon-button
                                           :md-icon-name "zmdi-book"
                                           :tooltip "Recipes"
                                           :tooltip-position :below-right
                                           :on-click #(dispatch [:state/update :active-main-pane :recipes])]
                                          [rc/md-circle-icon-button
                                           :md-icon-name "zmdi-shopping-cart"
                                           :tooltip "Ingredients"
                                           :tooltip-position :below-right
                                           :on-click #(dispatch [:state/update :active-main-pane :ingredients])]
                                          [rc/md-circle-icon-button
                                           :md-icon-name "zmdi-search"
                                           :tooltip "State"
                                           :tooltip-position :below-right
                                           :on-click #(dispatch [:state/update :active-main-pane :db-state])]]]
                              (if @active-main-pane
                                [(@active-main-pane main-panes)]
                                [recipes-section])]]]])))
