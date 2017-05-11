(ns user
  (:require [mount.core :as mount]
            copa.core
    ;[copa.handler :refer [app init destroy]]
    ;[luminus.http-server :as http]
    ;[config.core :refer [env]]
            ))

;(defn start []
;  (http/start {:handler app
;               :init    init
;               :port    (:port env)}))
;
;(defn stop []
;  (http/stop destroy))
;
;(defn restart []
;  (stop)
;  (start))


(defn start []
  (mount/start-without #'copa.core/repl-server))

(defn stop []
  (mount/stop-except #'copa.core/repl-server))

(defn restart []
  (stop)
  (start))