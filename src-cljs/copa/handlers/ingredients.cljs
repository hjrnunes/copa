(ns copa.handlers.ingredients
  (:require [copa.db :refer [default-value app-schema]]
            [copa.ajax :refer [load-auth-interceptor!]]
            [re-frame.core :refer [reg-event-fx reg-event-db path trim-v after]]
            [day8.re-frame.http-fx]
            [schema.core :as s]
            [plumbing.core :refer [map-vals]]
            [ajax.core :refer [json-response-format]]))

;; get ingredients
(reg-event-fx
  :get/ingredients
  (fn [_ _]
    {:http-xhrio {:method          :get
                  :uri             (str js/context "/api/ingredients")
                  :response-format (json-response-format {:keywords? true}) ;; IMPORTANT!: You must provide this.
                  :on-success      [:response/get-ingredients]
                  :on-failure      [:data/error]}
     :dispatch   [:loading/start]}))

;; get ingredients response
(reg-event-fx
  :response/get-ingredients
  (fn [{:keys [db]} [_ data]]
    {:db       (-> db
                   (assoc-in [:data :ingredients] data)
                   (assoc-in [:index :ingredients] (map-vals first
                                                             (group-by :ingredient_id data))))
     :dispatch [:loading/stop]}))

;; select ingredient
(reg-event-db
  :ingredient/select
  (fn [db [_ selected]]
    (-> db
        (assoc-in [:state :selected-ingredient] selected)
        (assoc-in [:state :active-ingredient-pane] :ingredient-details))))