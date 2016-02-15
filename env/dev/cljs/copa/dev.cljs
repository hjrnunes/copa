(ns ^:figwheel-no-load copa.app
  (:require [copa.core :as core]
            [figwheel.client :as figwheel :include-macros true]))

(enable-console-print!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  ;:on-jsload core/mount-components
  )

(core/init!)
