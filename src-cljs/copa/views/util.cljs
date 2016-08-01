(ns copa.views.util
  (:require-macros [copa.macros :refer [handler-fn]]
                   [reagent-forms.macros :refer [render-element]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent-forms.core :refer [init-field bind format-value value-of format-type]]
            [clojure.string :refer [join capitalize]]
            [plumbing.core :refer [indexed]]
            [json-html.core :refer [edn->hiccup]]))

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

(defn menu-button [icon color desc on-click]
  (let [mouse-over? (r/atom false)]
    (fn []
      [:span
       {:data-tooltip desc}
       [:div.ui.icon.item
        {:on-mouse-over (handler-fn (reset! mouse-over? true))
         :on-mouse-out  (handler-fn (reset! mouse-over? false))
         :on-click      on-click}
        [icon
         (-> (merge (if @mouse-over?
                      {:class color}
                      {:class "disabled"})))]]])))
