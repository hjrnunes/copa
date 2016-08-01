(ns copa.handlers.user
  (:require [copa.db :refer [default-value app-schema]]
            [copa.ajax :refer [load-auth-interceptor!]]
            [re-frame.core :refer [register-handler dispatch path trim-v after]]
            [schema.core :as s]
            [plumbing.core :refer [map-vals]]
            [ajax.core :refer [GET POST]]))

;; get users
(register-handler
  :get/users
  (fn [db _]
    (GET (str js/context "/api/admin/users")
         {:response-format :json
          :keywords?       true
          :handler         #(dispatch [:response/get-users %1])
          :error-handler   #(dispatch [:data/error %1])})
    (dispatch [:loading/start])
    db))

;; get users response
(register-handler
  :response/get-users
  (fn [db [_ data]]
    (dispatch [:loading/stop])
    (-> db
        (assoc-in [:index :users] (map-vals first
                                            (group-by :username data))))))

;; select user
(register-handler
  :user/select
  (fn [db [_ selected]]
    (-> db
        (assoc-in [:state :selected-user] selected))))
