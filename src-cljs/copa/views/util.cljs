(ns copa.views.util
  (:require-macros [copa.macros :refer [handler-fn]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [re-com.core :as rc :refer-macros [handler-fn]]
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
