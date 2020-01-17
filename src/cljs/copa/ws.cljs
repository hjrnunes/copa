(ns copa.ws
  (:require
    [re-frame.core :as rf]
    [cljs.core.async :as async :refer [<! >! put! chan]]
    [taoensso.sente :as sente :refer [cb-success?]]
    [taoensso.timbre :as timbre :refer-macros [debugf]])
  (:require-macros
    [cljs.core.async.macros :as asyncm :refer [go go-loop]]))


;; SENTE SOCKET

(defonce sente-socket
         (sente/make-channel-socket! "/chsk"                ; Note the same path as before
                                     js/csrfToken
                                     {:type :auto           ; e/o #{:auto :ajax :ws}
                                      }))

(let [{:keys [chsk ch-recv send-fn state]} sente-socket]
  (def chsk chsk)
  (def ch-chsk ch-recv)                                     ; ChannelSocket's receive channel
  (def chsk-send! send-fn)                                  ; ChannelSocket's send API fn
  (def chsk-state state)                                    ; Watchable, read-only atom
  )



(rf/reg-fx
  :ws/dispatch
  (fn [request-map]
    (chsk-send! request-map)))

(rf/reg-fx
  :ws/dispatch-n
  (fn [request-seq]
    (doseq [request request-seq]
      (chsk-send! request))))

(rf/reg-event-fx
  :chsk/ws-ping
  (fn [_ _] {}))

(defmulti event-msg-handler :id)

(defmethod event-msg-handler :copa.sync.client/recv-remote-tx
  [{:as ev-msg :keys [event]}]
  (js/console.log "Remote TX: %s" event))


(defmethod event-msg-handler :default
  [{:as ev-msg :keys [event]}]
  (js/console.log "Unandled event: %s" event)
  (js/console.log "EV-MSG: %s" ev-msg))

(defmethod event-msg-handler :chsk/state
  [{:as ev-msg :keys [?data]}]
  (let [[old-state-map new-state-map] ?data]
    (when (:first-open? new-state-map)
      (js/console.log "Channel socket successfully established!")
      (rf/dispatch [:copa.sync.client/request-bootstrap]))
    (js/console.log "Channel socket state change: %s" new-state-map)))

(defmethod event-msg-handler :chsk/handshake
  [{:as ev-msg :keys [?data]}]
  ; ignore
  )

(defmethod event-msg-handler :chsk/recv
  [{:as ev-msg :keys [?data]}]
  (js/console.log ev-msg)
  (js/console.log ?data)
  (rf/dispatch ?data))


(defn event-msg-handler* [{:as ev-msg :keys [id ?data event]}]
  (event-msg-handler ev-msg))


(def router (atom nil))

(defn stop-router! [] (when-let [stop-f @router] (stop-f)))

(defn start-router! []
  (stop-router!)
  (reset! router (sente/start-chsk-router! ch-chsk event-msg-handler*)))