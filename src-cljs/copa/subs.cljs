(ns copa.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

;; -- Helpers -----------------------------------------------------------------

;; -- Subscription handlers and registration  ---------------------------------

(register-sub
  :recipes
  (fn [db _]
    (reaction (:recipes @db))))