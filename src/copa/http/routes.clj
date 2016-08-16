(ns copa.http.routes
  (:require [copa.layout :as layout]
            [compojure.core :refer [defroutes context GET ANY]]
            [ring.util.http-response :refer [ok]]
            [clojure.java.io :as io]))

(defn index []
  (layout/render "home.html"))

(defroutes home-routes
           (GET "/" [] (index))
           (GET "/r" [] (index))
           (GET "/r/new" [] (index))
           (GET "/r/:id" [id] (index))
           (GET "/r/:id/edit" [id] (index))
           (GET "/i" [] (index))
           (GET "/i/:id" [id] (index))
           (GET "/u" [] (index))
           (GET "/state" [] (index)))

