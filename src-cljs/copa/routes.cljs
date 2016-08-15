(ns copa.routes
  (:require
    [re-frame.core :refer [dispatch dispatch-sync]]
    [bidi.bidi :as bidi]
    [pushy.core :as pushy]))

;; -- Routes and History ------------------------------------------------------

(def routes ["/" {""      :home
                  "r"     {""        :recipes
                           "/"       :recipes
                           ["/" :id] :recipes}
                  "i"     {""  :ingredients
                           "/" :ingredients}
                  "u"     {""  :user
                           "/" :user}
                  "state" {""  :db-state
                           "/" :db-state}}])

(defn- parse-url [url]
  (bidi/match-route routes url))

(defn- dispatch-route [{:keys [handler route-params]}]
  (dispatch [:state/update :active-main-pane handler]))

(def history (pushy/pushy dispatch-route parse-url))

(defn app-routes []
  (pushy/start! history))

(def url-for (partial bidi/path-for routes))

(defn set-current-path []
  (let [current-path (:pathname (js->clj (.. js/window -location)))]
    (pushy/set-token! history current-path)))