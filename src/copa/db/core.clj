(ns copa.db.core
  (:require
    [conman.core :as conman]
    [mount.core :refer [defstate]]
    [config.core :refer [env]]
    [taoensso.timbre :as timbre]))


(timbre/refer-timbre)

(defn init-db []
  (info "Connecting to DB " (env :database-url))
  (conman/connect! {:jdbc-url (env :database-url)}))

(defstate ^:dynamic *db*
          :start (init-db)
          :stop (conman/disconnect! *db*))

(conman/bind-connection *db*
                        "sql/users.sql"
                        "sql/ingredients.sql"
                        "sql/measurements.sql"
                        "sql/recipes.sql"
                        "sql/recipe-measurements.sql")


