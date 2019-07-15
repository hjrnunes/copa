(ns copa.routes.home
  (:require
    [copa.layout :as layout]
    [copa.db.core :as db]
    [clojure.java.io :as io]
    [copa.middleware :as middleware]
    [ring.util.http-response :as response]))

(defn home-page [request]
  (layout/render request "home.html"))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/docs" {:get (fn [_]
                    (-> (response/ok "")
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]])

