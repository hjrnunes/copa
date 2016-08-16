(ns copa.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub subscribe]]))

;; -- Subscription handlers and registration  ---------------------------------

;; subscription for the whole db for debug purposes
(register-sub
  :db
  (fn [db _]
    (reaction @db)))

;; generic state subscription
(register-sub
  :state
  (fn [db [_ field]]
    (reaction (get-in @db [:state field]))))

;; generic settings subscription
(register-sub
  :settings
  (fn [db [_ field]]
    (reaction (keyword (get-in @db [:settings field])))))

(register-sub
  :lang
  (fn [db _]
    (reaction (keyword (get-in @db [:state :user :lang] :en)))))

;; generic data subscription
(register-sub
  :data
  (fn [db [_ field]]
    (reaction (get-in @db [:data field]))))

;; generic index subscription
(register-sub
  :index
  (fn [db [_ field]]
    (reaction (get-in @db [:index field]))))

;; loading subscription
(register-sub
  :loading
  (fn [db _]
    (reaction (not (= (get-in @db [:state :loading]) 0)))))

;; alert subscription
(register-sub
  :alert
  (fn [db [_ field]]
    (reaction (get-in @db [:state :alert field]))))

; selected recipe in menu
(register-sub
  :state/selected-recipe
  (fn [db _]
    (let [selected (reaction (get-in @db [:state :selected-recipe]))
          index (subscribe [:index :recipes])]
      (reaction (get @index @selected)))))

; selected ingredient in menu
(register-sub
  :state/selected-ingredient
  (fn [db _]
    (let [selected (reaction (get-in @db [:state :selected-ingredient]))
          index (subscribe [:index :ingredients])]
      (reaction (get @index @selected)))))

; selected user in admin users menu
(register-sub
  :state/selected-user
  (fn [db _]
    (let [selected (reaction (get-in @db [:state :selected-user]))
          index (subscribe [:index :users])]
      (reaction (get @index @selected)))))

; sorted ingredients
(register-sub
  :sorted/ingredients
  (fn [db _]
    (let [ingredients (reaction (get-in @db [:data :ingredients]))]
      (reaction (sort-by :name @ingredients)))))

; sorted recipes
(register-sub
  :sorted/recipes
  (fn [db _]
    (let [index (subscribe [:index :recipes])
          recipes (reaction (vals @index))]
      (reaction (sort-by :name @recipes)))))

; sorted users
(register-sub
  :sorted/users
  (fn [db _]
    (let [index (subscribe [:index :users])
          recipes (reaction (vals @index))]
      (reaction (sort-by :username @recipes)))))

; recipes of user
(register-sub
  :user/recipes
  (fn [db [_ user]]
    (let [index (subscribe [:index :recipes])
          recipes (reaction (vals @index))
          user-recps (reaction (filter #(= (name user) (:user %))) @recipes)]
      (reaction (sort-by :name @user-recps)))))