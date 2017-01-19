(ns copa.handlers.ingredients
  (:require [re-frame.core :refer [reg-event-fx reg-event-db]]
            [day8.re-frame.http-fx]
            [plumbing.core :refer [map-vals]]
            [ajax.core :refer [json-response-format]]
            [copa.util :refer [common-interceptors]]))

;; get ingredients
(reg-event-fx
  :get/ingredients
  common-interceptors
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
  common-interceptors
  (fn [{:keys [db]} [data]]
    {:db       (-> db
                   (assoc-in [:data :ingredients] data)
                   (assoc-in [:index :ingredients] (map-vals first
                                                             (group-by :ingredient_id data))))
     :dispatch [:loading/stop]}))

;; select ingredient
(reg-event-db
  :ingredient/select
  common-interceptors
  (fn [db [selected]]
    (-> db
        (assoc-in [:state :selected-ingredient] selected)
        (assoc-in [:state :active-ingredient-pane] :ingredient-details))))