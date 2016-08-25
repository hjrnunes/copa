(ns copa.env
  (:require [selmer.parser :as parser]
            [taoensso.timbre :as timbre]
            [copa.dev-middleware :refer [wrap-dev]]))

(timbre/refer-timbre)

(def defaults
  {:init       (fn []
                 (parser/cache-off!)
                 (info "\n-=[copa started successfully using the development profile]=-"))
   :stop
               (fn []
                 (info "\n-=[copa has shut down successfully]=-"))
   :middleware wrap-dev})
