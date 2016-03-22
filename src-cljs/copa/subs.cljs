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

;; generic form state subscription
(register-sub
  :form-state
  (fn [db [_ form field]]
    (println "FORM: " (get-in @db [:state :forms form]))
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
