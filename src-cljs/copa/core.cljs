(ns copa.core
  (:require-macros [secretary.core :refer [defroute]])
  (:require [goog.events :as events]
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [secretary.core :as secretary]
            [copa.ajax :refer [load-interceptors! load-auth-interceptor!]]
            [copa.views.core :refer [copa-app]]
            [copa.handlers.core]
            [copa.subs])
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
  (load-interceptors!)
  (dispatch-sync [:state/update :force-login true])
  (when-let [token (.getItem js/localStorage "copa-token")]
    (dispatch-sync [:state/update :force-login false])
    (load-auth-interceptor! token)
    (dispatch [:get/settings])
    (dispatch [:get/recipes])
    (dispatch [:get/ingredients]))
  (reagent/render [copa-app]
                  (.getElementById js/document "app")))
