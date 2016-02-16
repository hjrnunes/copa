(ns copa.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

;; -- Helpers -----------------------------------------------------------------

;; -- Subscription handlers and registration  ---------------------------------

; active pane
(register-sub
  :active-pane
  (fn [db _]
    (reaction (get-in @db [:state :active-pane]))))

; all recipes
(register-sub
  :recipes
  (fn [db _]
    (reaction (:recipes @db))))

; selected recipe in menu
(register-sub
  :selected-recipe
  (fn [db _]
    (let [selected (reaction (get-in @db [:state :selected-recipe]))
          index (reaction (:index @db))]
      (reaction (get @index @selected)))))

; form input
(register-sub
  :form-input
  (fn [db [_ form field]]
    (reaction (get-in @db [:state :forms form field]))))

(register-sub
  :show-new-measurement
  (fn [db [_ form]]
    (reaction (get-in @db [:state :forms form :show-new-measurement]))))