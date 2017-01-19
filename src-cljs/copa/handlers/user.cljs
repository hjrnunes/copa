(ns copa.handlers.user
  (:require [copa.db :refer [default-value app-schema]]
            [copa.ajax :refer [load-auth-interceptor!]]
            [re-frame.core :refer [reg-event-fx reg-event-db path trim-v after]]
            [day8.re-frame.http-fx]
            [plumbing.core :refer [map-vals]]
            [hodgepodge.core :refer [local-storage]]
            [ajax.core :refer [json-request-format json-response-format]]))

;; get users
(reg-event-fx
  :get/users
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
  (fn [{:keys [db]} [_ data]]
    {:db       (assoc-in db [:index :users] (map-vals first
                                                      (group-by :username data)))
     :dispatch [:loading/stop]}))

;; select user
(reg-event-db
  :user/select
  (fn [db [_ selected]]
    (-> db
        (assoc-in [:state :selected-user] selected))))

;; save user
(reg-event-fx
  :user/save
  (fn [_ [_ form]]
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
  (fn [{:keys [db]} [_ data]]
    (let [username (:username data)]
      {:db         (assoc-in db [:index :users username] data)
       :dispatch-n [[:loading/stop]
                    [:user/select username]]})))

;; delete user
(reg-event-fx
  :user/delete
  (fn [_ [_ username]]
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
  (fn [{:keys [db]} [_ data]]
    (let [username (:username data)]
      {:db         (assoc-in db [:index :users] (dissoc (get-in db [:index :users]) username))
       :dispatch-n [[:loading/stop]
                    [:user/select nil]]})))

;; update language pref
(reg-event-fx
  :user/update-lang
  (fn [_ [_ username lang]]
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
  (fn [{:keys [db]} [_ data]]
    (let [username (:username data)]
      {:db         (-> db
                       (assoc-in [:index :users username] data)
                       (assoc-in [:state :user] data))
       :store-user data
       :dispatch   [:loading/stop]})))

;; update password
(reg-event-fx
  :user/update-password
  (fn [_ [_ username current new confirm]]
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
  (fn [{:keys [db]} [_ data]]
    (let [username (:username data)]
      {:dispatch [:loading/stop]})))