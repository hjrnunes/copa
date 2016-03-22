(ns copa.http.routes
  (:require [copa.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :refer [ok]]
            [clojure.java.io :as io]))

(defn index []
  (layout/render "home.html"))

(defroutes home-routes
  (GET "/" [] (index))
  (GET "/docs" [] (ok (-> "docs/docs.md" io/resource slurp))))

