(ns copa.handlers.ingredients
  (:require [copa.db :refer [default-value app-schema]]
            [copa.ajax :refer [load-auth-interceptor!]]
            [re-frame.core :refer [register-handler dispatch path trim-v after]]
            [schema.core :as s]
            [plumbing.core :refer [map-vals]]
            [ajax.core :refer [GET POST]]))

;; get ingredients
(register-handler
  :get/ingredients
  (fn [db _]
    (GET (str js/context "/api/ingredients")
         {:response-format :json
          :keywords?       true
          :handler         #(dispatch [:response/get-ingredients %1])
          :error-handler   #(dispatch [:data/error %1])})
    (dispatch [:loading/start])
    db))

;; get ingredients response
(register-handler
  :response/get-ingredients
  (fn [db [_ data]]
    (dispatch [:loading/stop])
    (-> db
        (assoc-in [:data :ingredients] data)
        (assoc-in [:index :ingredients] (map-vals first
                                                  (group-by :ingredient_id data))))))

;; select ingredient
(register-handler
  :ingredient/select
  (fn [db [_ selected]]
    (-> db
        (assoc-in [:state :selected-ingredient] selected)
        (assoc-in [:state :active-ingredient-pane] :ingredient-details))))