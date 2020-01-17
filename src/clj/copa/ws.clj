(ns copa.ws
  (:require
    [ring.middleware.keyword-params :refer [wrap-keyword-params]]
    [ring.middleware.params :refer [wrap-params]]
    [ring.util.http-response :refer :all]
    [taoensso.sente :as sente]
    [taoensso.sente.server-adapters.immutant :refer [get-sch-adapter]]
    [mount.core :as mount]
    [clojure.tools.logging :as log]
    [copa.db.core :as db]))


;; (timbre/set-level! :trace) ; Uncomment for more logging
(reset! sente/debug-mode?_ true)                            ; Uncomment for extra debug info

(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! (get-sch-adapter) {:user-id-fn (fn [_] :user)})]

  (def ring-ajax-post ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk ch-recv)                                     ; ChannelSocket's receive channel
  (def chsk-send! send-fn)                                  ; ChannelSocket's send API fn
  (def connected-uids connected-uids)                       ; Watchable, read-only atom
  )

(defn ws-routes []
  ["/chsk"
   {
    ;:middleware [wrap-keyword-params
    ;             wrap-params]
    :get  {:handler ring-ajax-get-or-ws-handshake}
    :post {:handler ring-ajax-post}}
   ])

;; We can watch this atom for changes if we like
(add-watch connected-uids :connected-uids
           (fn [_ _ old new]
             (when (not= old new)
               (log/infof "Connected uids change: %s" new))))

;;;; Sente event handlers

(defmulti -event-msg-handler
          "Multimethod to handle Sente `event-msg`s"
          :id                                               ; Dispatch on event-id
          )

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg)                               ; Handle event-msgs on a single thread
  ;; (future (-event-msg-handler ev-msg)) ; Handle event-msgs on a thread pool
  )

; Default/fallback case (no other matching handler)
(defmethod -event-msg-handler :default
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid (:uid session)]
    (log/debugf "Unhandled event: %s" event)
    (log/debugf "Session: %s" session)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-server event}))))


(defmethod -event-msg-handler :copa.sync.client/request-bootstrap
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid (:uid session)]
    (log/debugf "Request bootstrap: %s" event)
    (log/debugf "Session: %s" session)
    (let [bs (db/bootstrap)]
      (chsk-send! :user [:copa.sync.client/bootstrap bs]))))


(defmethod -event-msg-handler :copa.sync.client/tx
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid (:uid session)]
    (log/debugf "TX: %s" event)
    (log/debugf "Session: %s" session)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-server event}))))


;(defmethod -event-msg-handler :example/test-rapid-push
;  [ev-msg] (test-fast-server>user-pushes))

;(defmethod -event-msg-handler :example/toggle-broadcast
;  [{:as ev-msg :keys [?reply-fn]}]
;  (let [loop-enabled? (swap! broadcast-enabled?_ not)]
;    (?reply-fn loop-enabled?)))

;; TODO Add your (defmethod -event-msg-handler <event-id> [ev-msg] <body>)s here...

;;;; Sente event router (our `event-msg-handler` loop)

(mount/defstate ^:dynamic ws
  :start (sente/start-server-chsk-router!
           ch-chsk event-msg-handler)

  :stop (ws))