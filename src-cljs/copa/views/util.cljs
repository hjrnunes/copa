(ns copa.views.util
  (:require-macros [copa.macros :refer [handler-fn]]
                   [reagent-forms.macros :refer [render-element]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [re-com.core :as rc :refer-macros [handler-fn]]
            [reagent-forms.core :refer [init-field bind format-value value-of format-type]]
            [clojure.string :refer [join capitalize]]
            [plumbing.core :refer [indexed]]
            [json-html.core :refer [edn->hiccup]]))

(defn wired-textbox [{:keys [label form key textarea width password placeholder]
                      :or   [:label nil :textarea false :width "250px" :password false :placeholder ""]}]
  (let [model (subscribe [:form-state form key])]
    (fn []
      [rc/v-box
       :children
       [(when label
          [rc/label :label label])
        [(if textarea
           rc/input-textarea
           rc/input-text)
         :attr {:type (if password "password" "text")}
         :placeholder placeholder
         :width width
         :model (str (or @model ""))
         :change-on-blur? true
         :on-change #(dispatch [:form-state/update form key %])]]])))

(defmethod bind :text_area
  [{:keys [field id fmt]} {:keys [get save! doc]}]
  {:value     (let [value (or (get id) "")]
                (format-value fmt value))
   :on-change #(save! id (->> % (value-of) (format-type field)))})

(defn- set-attrs
  [[type attrs & body] opts & [default-attrs]]
  (into [:textarea (merge default-attrs (bind attrs opts) attrs)] body))

(defmethod init-field :text_area
  [[_ {:keys [field] :as attrs} :as component] {:keys [doc] :as opts}]
  (render-element attrs doc
                  (set-attrs component opts {:type :textarea})))
