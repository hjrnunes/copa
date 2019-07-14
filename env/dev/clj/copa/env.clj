(ns copa.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [copa.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[copa started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[copa has shut down successfully]=-"))
   :middleware wrap-dev})
