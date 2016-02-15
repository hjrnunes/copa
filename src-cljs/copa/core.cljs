(ns copa.core
  (:require-macros [secretary.core :refer [defroute]])
  (:require [goog.events :as events]
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [secretary.core :as secretary]
            [copa.handlers]
            [copa.subs]
            [copa.views])
  (:import [goog History]
           [goog.history EventType]))

;; -- Routes and History ------------------------------------------------------

(defroute "/" [])
;(defroute "/:filter" [filter] (dispatch [:set-showing (keyword filter)]))

(def history
  (doto (History.)
    (events/listen EventType.NAVIGATE
                   (fn [event] (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -- Entry Point -------------------------------------------------------------

(defn init! []
  (dispatch-sync [:load-data])
  (reagent/render [copa.views/copa-app]
                  (.getElementById js/document "app")))
