(ns copa.views.user
  (:require-macros [copa.macros :refer [handler-fn]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent-forms.core :refer [bind-fields]]
            [clojure.string :refer [join capitalize lower-case]]
            [markdown.core :refer [md->html]]
            [plumbing.core :refer [indexed]]
            [json-html.core :refer [edn->hiccup]]
            [copa.views.util :refer [wired-textbox]]
            [copa.util :refer [vec-remove]]))


(defn user-section []
  (let [user (subscribe [:state :user])
        recipes (subscribe [:user/recipes (:username @user)])]
    (fn []
      [:div.row
       [:h3.ui.top.attached.header
        (:username @user)]
       [:div.ui.attached.segment
        [:div.ui.list
         [:div.item
          [:div.ui.label
           "Recipes"
           [:div.detail (count @recipes)]]]
         [:div.item
          [:div.ui.label
           "Admin"
           [:div.detail (str (get @user :admin false))]]]]]])))

