(ns copa.routes.services
  (:require
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [copa.middleware.formats :as formats]
    [copa.middleware.exception :as exception]
    [ring.middleware.keyword-params :refer [wrap-keyword-params]]
    [ring.middleware.params :refer [wrap-params]]
    [ring.util.http-response :refer :all]
    [clojure.java.io :as io]
    [taoensso.sente :as sente]
    [taoensso.sente.server-adapters.immutant :refer (get-sch-adapter)]))

(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! (get-sch-adapter) {})]

  (def ring-ajax-post ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk ch-recv)                                     ; ChannelSocket's receive channel
  (def chsk-send! send-fn)                                  ; ChannelSocket's send API fn
  (def connected-uids connected-uids)                       ; Watchable, read-only atom
  )

(defn ws-routes []
  ["/chsk"
   {:middleware [wrap-keyword-params
                 wrap-params]
    :get        {:handler ring-ajax-get-or-ws-handshake}
    :post       {:handler ring-ajax-post}}

   ])

(defn service-routes []
  ["/api"
   {:coercion   spec-coercion/coercion
    :muuntaja   formats/instance
    :swagger    {:id ::api}
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]}

   ;; swagger documentation
   ["" {:no-doc  true
        :swagger {:info {:title       "my-api"
                         :description "https://cljdoc.org/d/metosin/reitit"}}}

    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
             {:url    "/api/swagger.json"
              :config {:validator-url nil}})}]]

   ["/v1"
    {:swagger {:tags ["v1"]}}
    ]




   ;["/math"
   ; {:swagger {:tags ["math"]}}
   ;
   ; ["/plus"
   ;  {:get  {:summary    "plus with spec query parameters"
   ;          :parameters {:query {:x int?, :y int?}}
   ;          :responses  {200 {:body {:total pos-int?}}}
   ;          :handler    (fn [{{{:keys [x y]} :query} :parameters}]
   ;                        {:status 200
   ;                         :body   {:total (+ x y)}})}
   ;   :post {:summary    "plus with spec body parameters"
   ;          :parameters {:body {:x int?, :y int?}}
   ;          :responses  {200 {:body {:total pos-int?}}}
   ;          :handler    (fn [{{{:keys [x y]} :body} :parameters}]
   ;                        {:status 200
   ;                         :body   {:total (+ x y)}})}}]]

   ;["/files"
   ; {:swagger {:tags ["files"]}}
   ;
   ; ["/upload"
   ;  {:post {:summary    "upload a file"
   ;          :parameters {:multipart {:file multipart/temp-file-part}}
   ;          :responses  {200 {:body {:name string?, :size int?}}}
   ;          :handler    (fn [{{{:keys [file]} :multipart} :parameters}]
   ;                        {:status 200
   ;                         :body   {:name (:filename file)
   ;                                  :size (:size file)}})}}]
   ;
   ; ["/download"
   ;  {:get {:summary "downloads a file"
   ;         :swagger {:produces ["image/png"]}
   ;         :handler (fn [_]
   ;                    {:status  200
   ;                     :headers {"Content-Type" "image/png"}
   ;                     :body    (-> "public/img/warning_clojure.png"
   ;                                  (io/resource)
   ;                                  (io/input-stream))})}}]]

   ])
