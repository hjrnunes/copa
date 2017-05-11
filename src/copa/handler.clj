(ns copa.handler
  (:require [compojure.core :refer [defroutes routes wrap-routes]]
            [copa.layout :refer [error-page]]
            [copa.http.routes :refer [home-routes]]
            [copa.http.api :refer [service-routes]]
            [copa.middleware :as middleware]
            [taoensso.timbre :as timbre]
            [compojure.route :as route]
            [config.core :refer [env]]
            [copa.env :refer [defaults]]
            [mount.core :as mount]
            [copa.db.mongo]))

(timbre/refer-timbre)

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    (var service-routes)
    (wrap-routes #'home-routes middleware/wrap-csrf)
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))

(def app (middleware/wrap-base #'app-routes))
