(ns copa.handlers
  (:require [copa.db :refer [default-value app-schema]]
            [re-frame.core :refer [register-handler dispatch path trim-v after]]
            [schema.core :as s]
            [ajax.core :refer [GET POST]]))

;; -- Middleware --------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/wiki/Using-Handler-Middleware
;;
(defn check-and-throw
  "throw an exception if db doesn't match the schema."
  [a-schema db]
  (if-let [problems (s/check a-schema db)]
    (println "DB:" db)
    (throw (js/Error. (str "schema check failed: " problems)))))

;; after an event handler has run, this middleware can check that
;; it the value in app-db still correctly matches the schema.
(def check-schema-mw (after (partial check-and-throw app-schema)))

;; -- Event Handlers ----------------------------------------------------------

;; load data request
(register-handler
  :load-data
  check-schema-mw
  (fn [db _]
    (GET (str js/context "/api/recipes")
         {:response-format :json
          :keywords?       true
          :handler         #(dispatch [:load-data-response %1])
          :error-handler   #(dispatch [:load-data-error %1])})
    (assoc-in db [:state :loading] true)))

;; load data response
(register-handler
  :load-data-response
  check-schema-mw
  (fn [db [_ data]]
    (-> db
        (assoc-in [:state :loading] true)
        (assoc :recipes data))))

;; load data error
(register-handler
  :load-data-error
  (fn [_ [data]]
    (println "Error:" data)))


