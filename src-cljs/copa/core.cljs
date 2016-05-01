(ns copa.core
  (:require-macros [secretary.core :refer [defroute]])
  (:require [goog.events :as events]
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [secretary.core :as secretary]
            [hodgepodge.core :refer [local-storage]]
            [copa.ajax :refer [load-interceptors! load-auth-interceptor!]]
            [copa.views.core :refer [copa-app]]
            [copa.handlers.core]
            [copa.subs])
  (:import [goog History]
           [goog.history EventType]))

;; -- Routes and History ------------------------------------------------------

(defroute "/" [])

(def history
  (doto (History.)
    (events/listen EventType.NAVIGATE
                   (fn [event] (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -- Entry Point -------------------------------------------------------------

(defn- load-data []
  (dispatch [:get/settings])
  (dispatch [:get/recipes])
  (dispatch [:get/ingredients]))

(defn init! []
  (load-interceptors!)
  (dispatch-sync [:state/update :force-login true])
  (when-let [token (get local-storage :copa-token)]
    (load-auth-interceptor! token)
    (dispatch-sync [:state/update :force-login false])
    (dispatch-sync [:state/update :user (get local-storage :copa-user)])
    (load-data))
  (reagent/render [copa-app]
                  (.getElementById js/document "app")))
