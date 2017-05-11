(ns copa.env
  (:require [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(def defaults
  {:init       (fn []
                 (info "\n-=[copa started successfully]=-"))
   :stop       (fn []
                 (info "\n-=[copa has shut down successfully]=-"))
   :middleware identity})
