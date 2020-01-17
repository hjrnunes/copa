(ns copa.sync
  (:require
    [copa.db :refer [tempid]]
    [re-posh.core :as rp]
    [datascript.core :as ds]
    [re-frame.core :as rf]))

(defn prep-bs-tx [data]
  (->> data
       (map #(assoc % :db/id (tempid)))))

(rf/reg-event-fx
  :copa.sync.client/request-bootstrap
  (fn [_ _]
    (js/console.log :copa.sync.client/request-bootstrap)
    {:ws/dispatch [:copa.sync.client/request-bootstrap]}))

(rp/reg-event-ds
  :copa.sync.client/bootstrap
  (fn [_ [_ bs-data]]
    (->> bs-data
         (prep-bs-tx))))
