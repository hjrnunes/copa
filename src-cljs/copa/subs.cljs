(ns copa.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

;; -- Helpers -----------------------------------------------------------------

;; -- Subscription handlers and registration  ---------------------------------

(register-sub
  :recipes
  (fn [db _]
    (reaction (:recipes @db))))

(register-sub
  :selected-recipe
  (fn [db _]
    (let [selected (reaction (get-in @db [:state :selected-recipe]))
          index (reaction (:index @db))]
      (reaction (get @index @selected)))))