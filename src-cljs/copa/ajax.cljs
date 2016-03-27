(ns copa.ajax
  (:require [ajax.core :as ajax]
            [cuerdas.core :as str]))

;(defn should-apply-csrf-token [request]
;  (and (not (= "GET" (:method request)))
;       (or (str/starts-with? (:uri request) "/api")
;           (str/starts-with? (:uri request) "/auth")
;           (str/starts-with? (:uri request) "/install"))))

(defn default-headers [request]
  (-> request
      (update :uri #(str js/context %))
      ;(update
      ;  :headers
      ;  #(merge
      ;    %
      ;    (when (should-apply-csrf-token request)
      ;      {"x-csrf-token" js/csrfToken})))
      ))

(defn load-interceptors! []
  (swap! ajax/default-interceptors
         conj
         (ajax/to-interceptor {:name    "default headers"
                               :request default-headers})))

(defn jws-header [token request]
  (if (str/starts-with? (:uri request) "/api")
    (-> request
        (update :headers #(merge % {"Authorization" (str "Token " token)})))
    request))

(defn load-auth-interceptor! [token]
  (swap! ajax/default-interceptors
         conj
         (ajax/to-interceptor {:name    "auth header"
                               :request #(jws-header token %)})))


