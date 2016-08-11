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
           (GET "/r/:slug" [slug] (index))
           (GET "/i" [] (index))
           (GET "/u" [] (index))
           (GET "/state" [] (index)))

