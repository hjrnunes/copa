(ns copa.routing
  (:require
    [re-frame.core :as rf]
    [reitit.core :as reitit]))

(def routes
  [["/" :route/home]
   ["/recipe/:id" :route/recipe]])

(defonce router (reitit/router routes))

(rf/reg-sub
  :nav/route
  :<- [:kee-frame/route]
  identity)

(rf/reg-sub
  :route/name
  :<- [:kee-frame/route]
  (fn [route _]
    (get-in route [:data :name])))

(rf/reg-event-fx
  :nav/route-name
  (fn [_ [_ route-name]]
    {:navigate-to [route-name]}))

(rf/reg-event-fx
  :nav/route-name-params
  (fn [_ [_ route-name params]]
    (let [route (reitit/match-by-name router route-name)]
      {:navigate-to [(-> route :data :name) params]})))

(rf/reg-sub
  :nav/page
  :<- [:nav/route]
  (fn [route _]
    (-> route :data :name)))
