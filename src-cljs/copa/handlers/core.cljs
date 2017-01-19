(ns copa.handlers.core
  (:require [copa.db :refer [default-value app-schema]]
            [copa.ajax :refer [load-auth-interceptor!]]
            [re-frame.core :refer [reg-event-db reg-event-fx reg-fx path trim-v after]]
            [day8.re-frame.http-fx]
            [plumbing.core :refer [map-vals]]
            [ajax.core :refer [json-response-format json-request-format]]
            [hodgepodge.core :refer [local-storage]]
            [copa.handlers.ingredients]
            [copa.handlers.recipes]
            [copa.handlers.user]
            [copa.routes :refer [push-url-for]]))

;; -- loading -----------------------------------------------------------

(reg-event-db
  :loading/start
  (fn [db _]
    (update-in db [:state :loading] (fnil inc 0))))

(reg-event-db
  :loading/stop
  (fn [db _]
    (if (= (get-in db [:state :loading]) 0)
      db
      (update-in db [:state :loading] (fnil dec 0)))))

(reg-fx
  :push-url
  (fn [[handler params]]
    (apply push-url-for handler params)))

(reg-event-fx
  :push-url-for
  (fn [_ [_ handler & params]]
    {:push-url [handler params]}))

(defn auth-error [db]
  (println "Authentication Error: token expired")
  (dissoc! local-storage :copa-token)
  (-> db
      (assoc-in [:state :alert] true)
      (assoc-in [:state :token] nil)
      (assoc-in [:state :force-login] true)))

(reg-event-fx
  :data/error
  (fn [{:keys [db]} [_ data]]
    (println "Error" data)
    (if (= (:status data) 403)
      {:db       (auth-error db)
       :dispatch [:loading/stop]}
      {:dispatch-n [[:loading/stop]
                    [:alert/show :negative (or (get-in data [:response :message]) (:status-text data))]]})))

(reg-event-db
  :alert/show
  (fn [db [_ type message]]
    (assoc-in db [:state :alert] {:type    type
                                  :message message})))

(reg-event-db
  :alert/hide
  (fn [db _]
    (assoc-in db [:state :alert] {})))

(reg-event-db
  :update/active-main-pane
  (fn [db [_ value]]
    (assoc-in db [:state :active-main-pane] value)))

(reg-event-db
  :update/active-recipe-pane
  (fn [db [_ value]]
    (assoc-in db [:state :active-recipe-pane] value)))

(reg-event-db
  :update/active-users-pane
  (fn [db [_ value]]
    (assoc-in db [:state :active-users-pane] value)))

(reg-event-db
  :update/user
  (fn [db [_ value]]
    (assoc-in db [:state :user] value)))

(reg-event-db
  :update/force-login
  (fn [db [_ value]]
    (assoc-in db [:state :force-login] value)))

;; login user
(reg-event-fx
  :data/login
  (fn [_ [_ form]]
    {:http-xhrio {:method          :post
                  :uri             (str js/context "/auth")
                  :params          {:username (:username form)
                                    :password (:password form)}
                  :format          (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success      [:response/login]
                  :on-failure      [:data/error]}
     :dispatch   [:loading/start]}))

(reg-fx
  :load-auth-interceptor
  (fn [token]
    (load-auth-interceptor! token)))

(reg-fx
  :store-token
  (fn [token]
    (assoc! local-storage :copa-token token)))

(reg-fx
  :store-user
  (fn [user]
    (assoc! local-storage :copa-user user)))

;; get auth token response
(reg-event-fx
  :response/login
  (fn [{:keys [db]} [_ data]]
    {:db                    (-> db
                                (assoc-in [:state :token] (:token data))
                                (assoc-in [:state :force-login] false)
                                (assoc-in [:state :user] (:user data)))
     :load-auth-interceptor (:token data)
     :store-token           (:token data)
     :store-user            (:user data)
     :dispatch-n            [[:loading/stop]
                             [:get/recipes]
                             [:get/ingredients]]}))