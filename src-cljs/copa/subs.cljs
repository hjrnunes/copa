(ns copa.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [reg-sub-raw reg-sub subscribe]]))

;; -- Subscription handlers and registration  ---------------------------------

;; subscription for the whole db for debug purposes
;(reg-sub-raw
;  :db
;  (fn [db _]
;    (reaction @db)))

;; Layer 2
(reg-sub
  :db
  (fn [db _]
    db))

(reg-sub
  :user
  (fn [db _]
    (get-in db [:state :user])))

;; alert subscription
(reg-sub
  :alert
  (fn [db _]
    (get-in db [:state :alert])))

(reg-sub
  :active-main-pane
  (fn [db _]
    (get-in db [:state :active-main-pane])))

(reg-sub
  :active-users-pane
  (fn [db _]
    (get-in db [:state :active-users-pane])))

(reg-sub
  :active-recipe-pane
  (fn [db _]
    (get-in db [:state :active-recipe-pane])))

(reg-sub
  :force-login
  (fn [db _]
    (get-in db [:state :force-login])))

(reg-sub
  :lang
  (fn [db _]
    (keyword (get-in db [:state :user :lang] :en))))

;; loading subscription
(reg-sub
  :loading?
  (fn [db _]
    (not (= (get-in db [:state :loading]) 0))))

(reg-sub
  :index/recipes
  (fn [db _]
    (get-in db [:index :recipes])))

(reg-sub
  :index/ingredients
  (fn [db _]
    (get-in db [:index :ingredients])))

(reg-sub
  :index/users
  (fn [db _]
    (get-in db [:index :users])))

(reg-sub
  :selected-recipe-id
  (fn [db _]
    (get-in db [:state :selected-recipe])))

(reg-sub
  :selected-ingredient-id
  (fn [db _]
    (get-in db [:state :selected-ingredient])))

(reg-sub
  :selected-user-id
  (fn [db _]
    (get-in db [:state :selected-user])))

(reg-sub
  :ingredients
  (fn [db _]
    (get-in db [:data :ingredients])))

;; Layer 3

;; alert subscription
(reg-sub
  :alert-type
  :<- [:alert]
  (fn [alert _]
    (:type alert)))

;; alert subscription
(reg-sub
  :alert-message
  :<- [:alert]
  (fn [alert _]
    (:message alert)))

; selected recipe in menu
(reg-sub
  :selected-recipe
  :<- [:selected-recipe-id]
  :<- [:index/recipes]
  (fn [[selected index] _]
    (get index selected)))

; selected ingredient in menu
(reg-sub
  :selected-ingredient
  :<- [:selected-ingredient-id]
  :<- [:index/ingredients]
  (fn [[selected index] _]
    (get index selected)))

; selected user in admin users menu
(reg-sub
  :selected-user
  :<- [:selected-user-id]
  :<- [:index/users]
  (fn [[selected index] _]
    (get index selected)))

; recipes of user
(reg-sub
  :user-recipes
  :<- [:index/users]
  :<- [:user]
  (fn [[index user] _]
    (filter #(= (name (:username user)) (:user %)) (vals index))))

; sorted recipes of user
(reg-sub
  :sorted-user-recipes
  :<- [:user-recipes]
  (fn [recipes _]
    (sort-by :name recipes)))

; sorted ingredients
(reg-sub
  :sorted-ingredients
  :<- [:ingredients]
  (fn [ingredients _]
    (sort-by :name ingredients)))

; sorted recipes
(reg-sub
  :sorted-recipes
  :<- [:index/recipes]
  (fn [index _]
    (sort-by :name (vals index))))

; sorted users
(reg-sub
  :sorted-users
  :<- [:index/users]
  (fn [index _]
    (sort-by :username (vals index))))

; recipes of user
;(reg-sub-raw
;  :user/recipes
;  (fn [db [_ user]]
;    (let [index (subscribe [:index/recipes])
;          recipes (reaction (vals @index))
;          user-recps (reaction (filter #(= (name user) (:user %))) @recipes)]
;      (reaction (sort-by :name @user-recps)))))