(ns copa.http.routes
  (:require [copa.layout :as layout]
            [copa.http.service :as s]
            [compojure.core :refer [defroutes context GET ANY]]
            [ring.util.http-response :refer [ok]]
            [ring.util.response :as r]))

(defn index []
  (layout/render "home.html"))

(defn export []
  (-> (s/get-all-recipes)
      (r/header "Content-Disposition" "attachment; filename='copa_export.json'")))

(defroutes home-routes
           (GET "/" [] (index))
           (GET "/r" [] (index))
           (GET "/r/new" [] (index))
           (GET "/r/:id" [id] (index))
           (GET "/r/:id/edit" [id] (index))
           (GET "/i" [] (index))
           (GET "/i/:id" [id] (index))
           (GET "/u" [] (index))
           (GET "/state" [] (index))
           (GET "/export" [] (export)))

