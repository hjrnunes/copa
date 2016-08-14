(ns copa.handlers.user
  (:require [copa.db :refer [default-value app-schema]]
            [copa.ajax :refer [load-auth-interceptor!]]
            [re-frame.core :refer [register-handler dispatch path trim-v after]]
            [plumbing.core :refer [map-vals]]
            [hodgepodge.core :refer [local-storage]]
            [ajax.core :refer [GET POST DELETE]]))

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

;; save user
(register-handler
  :user/save
  (fn [db [_ form]]
    (let []
      (POST (str js/context "/api/admin/users")
            {:response-format :json
             :params          form
             :keywords?       true
             :handler         #(dispatch [:response/user-save %1])
             :error-handler   #(dispatch [:data/error %1])})
      (dispatch [:loading/start])
      db)))

;; user post result
(register-handler
  :response/user-save
  (fn [db [_ data]]
    (let [username (:username data)
          db (-> db
                 ;(update-in [:data :recipes] conj data)
                 (assoc-in [:index :users username] data))]
      (dispatch [:loading/stop])
      (dispatch [:user/select username])
      db)))

;; delete user
(register-handler
  :user/delete
  (fn [db [_ username]]
    (let [params {:username username}]
      (DELETE (str js/context "/api/admin/users")
              {:response-format :json
               :params          params
               :keywords?       true
               :handler         #(dispatch [:response/user-delete %1])
               :error-handler   #(dispatch [:data/error %1])})
      (dispatch [:loading/start])
      db)))

;; user delete result
(register-handler
  :response/user-delete
  (fn [db [_ data]]
    (let [username (:username data)
          db (-> db
                 (assoc-in [:index :users] (dissoc (get-in db [:index :users]) username)))]
      (dispatch [:loading/stop])
      (dispatch [:user/select nil])
      db)))

;; update language pref
(register-handler
  :user/update-lang
  (fn [db [_ username lang]]
    (let [params {:lang     (name lang)
                  :username username}]
      (POST (str js/context "/api/user/lang")
            {:response-format :json
             :params          params
             :keywords?       true
             :handler         #(dispatch [:response/user-update-lang %1])
             :error-handler   #(dispatch [:data/error %1])})
      (dispatch [:loading/start])
      db)))

;; user delete result
(register-handler
  :response/user-update-lang
  (fn [db [_ data]]
    (let [username (:username data)
          db (-> db
                 (assoc-in [:index :users username] data)
                 (assoc-in [:state :user] data))]
      (assoc! local-storage :copa-user data)
      (dispatch [:loading/stop])
      db)))