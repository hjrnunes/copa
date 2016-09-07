(ns copa.handlers.core
  (:require [copa.db :refer [default-value app-schema]]
            [copa.ajax :refer [load-auth-interceptor!]]
            [re-frame.core :refer [register-handler dispatch path trim-v after]]
            [plumbing.core :refer [map-vals]]
            [ajax.core :refer [GET POST]]
            [hodgepodge.core :refer [local-storage]]
            [copa.handlers.ingredients]
            [copa.handlers.recipes]
            [copa.handlers.user]
            [copa.routes :refer [push-url-for]]))

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

(register-handler
  :push-url-for
  (fn [db [_ handler & params]]
    (apply push-url-for handler params)
    db))

(defn auth-error [db]
  (println "Authentication Error: token expired")
  (dissoc! local-storage :copa-token)
  (-> db
      (assoc-in [:state :alert] true)
      (assoc-in [:state :token] nil)
      (assoc-in [:state :force-login] true)))

;; load data error
(register-handler
  :data/error
  (fn [db [_ data]]
    (dispatch [:loading/stop])
    (println "Error" data)
    (if (= (:status data) 403)
      (auth-error db)
      (do
        (dispatch [:alert/show :negative (or (get-in data [:response :message]) (:status-text data))])
        db))))


;; -- alert -----------------------------------------------------------

(register-handler
  :alert/show
  (fn [db [_ type message]]
    (assoc-in db [:state :alert] {:type    type
                                  :message message})))

(register-handler
  :alert/hide
  (fn [db _]
    (assoc-in db [:state :alert] {})))

;; generic update state handler
(register-handler
  :state/update
  (fn [db [_ key value]]
    (assoc-in db [:state key] value)))

;; get settings
;(register-handler
;  :get/settings
;  (fn [db _]
;    (GET (str js/context "/api/settings")
;         {:response-format :json
;          :keywords?       true
;          :handler         #(dispatch [:response/get-settings %1])
;          :error-handler   #(dispatch [:data/error %1])})
;    (dispatch [:loading/start])
;    db))

;; get settings response
;(register-handler
;  :response/get-settings
;  (fn [db [_ data]]
;    (dispatch [:loading/stop])
;    (-> db
;        (assoc-in [:settings] data))))

;; login user
(register-handler
  :data/login
  (fn [db [_ form]]
    (POST (str js/context "/auth")
          {:params          {:username (:username form)
                             :password (:password form)}
           :response-format :json
           :keywords?       true
           :handler         #(dispatch [:response/login %1])
           :error-handler   #(dispatch [:data/error %1])})
    (dispatch [:loading/start])
    db))

(defn load-data! []
  (dispatch [:get/recipes])
  (dispatch [:get/ingredients]))

;; get auth token response
(register-handler
  :response/login
  (fn [db [_ data]]
    (load-auth-interceptor! (:token data))
    (load-data!)
    (assoc! local-storage :copa-token (:token data))
    (assoc! local-storage :copa-user (:user data))
    (dispatch [:loading/stop])
    (-> db
        (assoc-in [:state :token] (:token data))
        (assoc-in [:state :force-login] false)
        (assoc-in [:state :user] (:user data)))))