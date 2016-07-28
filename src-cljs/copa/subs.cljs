(ns copa.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

;; -- Helpers -----------------------------------------------------------------

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

;; generic form state subscription
(register-sub
  :form-state
  (fn [db [_ form field]]
    (reaction (get-in @db [:state :forms form field]))))

;; generic data subscription
(register-sub
  :data
  (fn [db [_ field]]
    (reaction (get-in @db [:data field]))))

;; loading subscription
(register-sub
  :loading
  (fn [db _]
    (reaction (not (= (get-in @db [:state :loading]) 0)))))

; selected recipe in menu
(register-sub
  :state/selected-recipe
  (fn [db _]
    (let [selected (reaction (get-in @db [:state :selected-recipe]))
          index (reaction (get-in @db [:index :recipes]))]
      (reaction (get @index @selected)))))

; selected ingredient in menu
(register-sub
  :state/selected-ingredient
  (fn [db _]
    (let [selected (reaction (get-in @db [:state :selected-ingredient]))
          index (reaction (get-in @db [:index :ingredients]))]
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
    (let [recipes (reaction (get-in @db [:data :recipes]))]
      (reaction (sort-by :name @recipes)))))