(ns copa.handler
  (:require [compojure.core :refer [defroutes routes wrap-routes]]
            [copa.layout :refer [error-page]]
            [copa.http.routes :refer [home-routes]]
            [copa.http.api :refer [service-routes]]
            [copa.middleware :as middleware]
            [taoensso.timbre :as timbre]
            [compojure.route :as route]
            [config.core :refer [env]]
            [copa.config :refer [defaults]]
            [mount.core :as mount]
            [copa.db.mongo]))

(timbre/refer-timbre)

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (info "INIT")
  ;(logger/init env)
  (doseq [component (:started (mount/start))]
    (info component "started"))
  ((:init defaults)))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (info "copa is shutting down...")
  (doseq [component (:stopped (mount/stop))]
    (info component "stopped"))
  (info "shutdown complete!"))

(def app-routes
  (routes
    (var service-routes)
    (wrap-routes #'home-routes middleware/wrap-csrf)
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))

(def app (middleware/wrap-base #'app-routes))
