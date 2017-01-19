(ns copa.handlers.user
  (:require [re-frame.core :refer [reg-event-fx reg-event-db]]
            [day8.re-frame.http-fx]
            [plumbing.core :refer [map-vals]]
            [ajax.core :refer [json-request-format json-response-format]]
            [copa.util :refer [common-interceptors]]))

;; get users
(reg-event-fx
  :get/users
  common-interceptors
  (fn [_ _]
    {:http-xhrio {:method          :get
                  :uri             (str js/context "/api/admin/users")
                  :response-format (json-response-format {:keywords? true})
                  :on-success      [:response/get-users]
                  :on-failure      [:data/error]}
     :dispatch   [:loading/start]}))

;; get users response
(reg-event-fx
  :response/get-users
  common-interceptors
  (fn [{:keys [db]} [data]]
    {:db       (assoc-in db [:index :users] (map-vals first
                                                      (group-by :username data)))
     :dispatch [:loading/stop]}))

;; select user
(reg-event-db
  :user/select
  common-interceptors
  (fn [db [selected]]
    (-> db
        (assoc-in [:state :selected-user] selected))))

;; save user
(reg-event-fx
  :user/save
  common-interceptors
  (fn [_ [form]]
    {:http-xhrio {:method          :post
                  :uri             (str js/context "/api/admin/users")
                  :params          form
                  :format          (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success      [:response/user-save]
                  :on-failure      [:data/error]}
     :dispatch   [:loading/start]}))

;; user post result
(reg-event-fx
  :response/user-save
  common-interceptors
  (fn [{:keys [db]} [data]]
    (let [username (:username data)]
      {:db         (assoc-in db [:index :users username] data)
       :dispatch-n [[:loading/stop]
                    [:user/select username]]})))

;; delete user
(reg-event-fx
  :user/delete
  common-interceptors
  (fn [_ [username]]
    (let [params {:username username}]
      {:http-xhrio {:method          :delete
                    :uri             (str js/context "/api/admin/users")
                    :params          params
                    :format          (json-request-format)
                    :response-format (json-response-format {:keywords? true})
                    :on-success      [:response/user-delete]
                    :on-failure      [:data/error]}
       :dispatch   [:loading/start]})))

;; user delete result
(reg-event-fx
  :response/user-delete
  common-interceptors
  (fn [{:keys [db]} [data]]
    (let [username (:username data)]
      {:db         (assoc-in db [:index :users] (dissoc (get-in db [:index :users]) username))
       :dispatch-n [[:loading/stop]
                    [:user/select nil]]})))

;; update language pref
(reg-event-fx
  :user/update-lang
  common-interceptors
  (fn [_ [username lang]]
    (let [params {:lang     (name lang)
                  :username username}]
      {:http-xhrio {:method          :post
                    :uri             (str js/context "/api/user/lang")
                    :params          params
                    :format          (json-request-format)
                    :response-format (json-response-format {:keywords? true})
                    :on-success      [:response/user-update-lang]
                    :on-failure      [:data/error]}
       :dispatch   [:loading/start]})))


;; user update lang result
(reg-event-fx
  :response/user-update-lang
  common-interceptors
  (fn [{:keys [db]} [data]]
    (let [username (:username data)]
      {:db         (-> db
                       (assoc-in [:index :users username] data)
                       (assoc-in [:state :user] data))
       :store-user data
       :dispatch   [:loading/stop]})))

;; update password
(reg-event-fx
  :user/update-password
  common-interceptors
  (fn [_ [username current new confirm]]
    (let [params {:username username
                  :current  current
                  :new      new
                  :confirm  confirm}]
      {:http-xhrio {:method          :post
                    :uri             (str js/context "/api/user/password")
                    :params          params
                    :format          (json-request-format)
                    :response-format (json-response-format {:keywords? true})
                    :on-success      [:response/user-update-password]
                    :on-failure      [:data/error]}
       :dispatch   [:loading/start]})))

;; update password result
(reg-event-fx
  :response/user-update-password
  common-interceptors
  (fn [{:keys [db]} [data]]
    (let [username (:username data)]
      {:dispatch [:loading/stop]})))