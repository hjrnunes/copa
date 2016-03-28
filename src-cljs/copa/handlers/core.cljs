(ns copa.handlers.core
  (:require [copa.db :refer [default-value app-schema]]
            [copa.ajax :refer [load-auth-interceptor!]]
            [re-frame.core :refer [register-handler dispatch path trim-v after]]
            [plumbing.core :refer [map-vals]]
            [ajax.core :refer [GET POST]]
            [copa.handlers.ingredients]
            [copa.handlers.recipes]))

;; -- loading -----------------------------------------------------------

(register-handler
  :loading/start
  (fn [db _]
    (update-in db [:state :loading] (fnil inc 0))))

(register-handler
  :loading/stop
  (fn [db _]
    (if (= (get-in db [:state :loading]) 0)
      db
      (update-in db [:state :loading] (fnil dec 0)))))

(defn auth-error [db]
  (println "Authentication Error: token expired")
  (.setItem js/localStorage "copa-token" nil)
  (-> db
      (assoc-in [:state :alert] true)
      (assoc-in [:state :token] nil)
      (assoc-in [:state :force-login] true)))

;; load data error
(register-handler
  :data/error
  (fn [db [_ data]]
    (dispatch [:loading/stop])
    (if (= (:status data) 403)
      (auth-error db)
      (do
        (println "Error:" data)
        db))))

;; generic update state handler
(register-handler
  :state/update
  (fn [db [_ key value]]
    (assoc-in db [:state key] value)))

;; generic update form state handler
(register-handler
  :form-state/update
  (fn [db [_ form key value]]
    (assoc-in db [:state :forms form key] value)))

;; form load for edit handler
(register-handler
  :form-state/load
  (fn [db [_ form data]]
    (assoc-in db [:state :forms form] data)))

;; get settings
(register-handler
  :get/settings
  (fn [db _]
    (GET (str js/context "/api/settings")
         {:response-format :json
          :keywords?       true
          :handler         #(dispatch [:response/get-settings %1])
          :error-handler   #(dispatch [:data/error %1])})
    (dispatch [:loading/start])
    db))

;; get settings response
(register-handler
  :response/get-settings
  (fn [db [_ data]]
    (dispatch [:loading/stop])
    (-> db
        (assoc-in [:settings] data))))

;; login user
(register-handler
  :data/login
  (fn [db [_ form]]
    (println (get-in db [:state :form]))
    (POST (str js/context "/auth")
          {:params          {:username (get-in db [:state :forms form :username])
                             :password (get-in db [:state :forms form :password])}
           :response-format :json
           :keywords?       true
           :handler         #(dispatch [:response/login %1])
           :error-handler   #(dispatch [:data/error %1])})
    ;(dispatch [:loading/start])
    db))

(defn load-data! []
  (dispatch [:get/settings])
  (dispatch [:get/recipes])
  (dispatch [:get/ingredients]))

;; get auth token response
(register-handler
  :response/login
  (fn [db [_ data]]
    (load-auth-interceptor! (:token data))
    (load-data!)
    (.setItem js/localStorage "copa-token" (:token data))
    (-> db
        (assoc-in [:state :token] (:token data))
        (assoc-in [:state :force-login] false)
        (assoc-in [:state :user] (:user data)))))