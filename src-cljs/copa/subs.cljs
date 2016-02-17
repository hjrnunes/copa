(ns copa.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

;; -- Helpers -----------------------------------------------------------------

;; -- Subscription handlers and registration  ---------------------------------

;; generic state subscription
(register-sub
  :state
  (fn [db [_ field]]
    (reaction (get-in @db [:state field]))))

;; generic form state subscription
(register-sub
  :form-state
  (fn [db [_ form field]]
    (reaction (get-in @db [:state :forms form field]))))

; all recipes
(register-sub
  :data/recipes
  (fn [db _]
    (reaction (:recipes @db))))

; selected recipe in menu
(register-sub
  :state/selected-recipe
  (fn [db _]
    (let [selected (reaction (get-in @db [:state :selected-recipe]))
          index (reaction (:index @db))]
      (reaction (get @index @selected)))))
