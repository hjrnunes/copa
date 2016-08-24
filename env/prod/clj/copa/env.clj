(ns copa.env
    (:require [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(def defaults
  {:init
   (fn []
     (info "\n-=[copa started successfully]=-"))
   :middleware identity})
