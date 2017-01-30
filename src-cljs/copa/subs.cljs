(ns copa.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [reg-sub-raw reg-sub subscribe]]))

;; -- Subscription handlers and registration  ---------------------------------

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
  :selected-ingredients-ids
  (fn [db _]
    (get-in db [:state :selected-ingredients-ids])))

(reg-sub
  :selected-user-id
  (fn [db _]
    (get-in db [:state :selected-user])))

(reg-sub
  :ingredients
  (fn [db _]
    (get-in db [:data :ingredients])))

(reg-sub
  :matched-recipes-ids
  (fn [db _]
    (get-in db [:state :matched-recipes-ids])))

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

; selected ingredients for matcher
(reg-sub
  :selected-ingredients
  :<- [:selected-ingredients-ids]
  :<- [:index/ingredients]
  (fn [[selected index] _]
    (vec (for [id selected]
           (get index id)))))

; selected user in admin users menu
(reg-sub
  :selected-user
  :<- [:selected-user-id]
  :<- [:index/users]
  (fn [[selected index] _]
    (get index selected)))

; matched recipes
(reg-sub
  :matched-recipes
  :<- [:matched-recipes-ids]
  :<- [:index/recipes]
  (fn [[selected index] _]
    (vec (for [id selected]
           (get index id)))))

; recipes of user
(reg-sub
  :user-recipes
  :<- [:index/recipes]
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
