(ns copa.handlers
  (:require [copa.db :refer [default-value app-schema]]
            [re-frame.core :refer [register-handler dispatch path trim-v after]]
            [schema.core :as s]
            [plumbing.core :refer [map-vals]]
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
        (assoc :recipes data)
        (assoc :index (map-vals first
                                (group-by :db/id data)))
        (assoc-in [:state :loading] false))))

;; load data error
(register-handler
  :load-data-error
  (fn [db [_ data]]
    (println "Error:" data)
    (assoc-in db [:state :loading] false)))

;; select recipe
(register-handler
  :select-recipe
  (fn [db [_ selected]]
    (-> db
        (assoc-in [:state :selected-recipe] selected)
        (assoc-in [:state :active-pane] :recipe-details))))

(register-handler
  :display-pane
  (fn [db [_ pane]]
    (assoc-in db [:state :active-pane] pane)))

;form input handler
(register-handler
  :form-input-changed
  (fn [db [_ form field value]]
    (assoc-in db [:state :forms form field] value)))

(register-handler
  :show-new-measurement
  (fn [db [_ form value]]
    (assoc-in db [:state :forms form :show-new-measurement] value)))

; update new recipe measurements list
(defn- build-tmp-measurement [db form]
  (let [tmp-ingredient (get-in db [:state :forms form :tmp.measurement/ingredient])
        tmp-unit (get-in db [:state :forms form :tmp.measurement/unit])
        tmp-quantity (get-in db [:state :forms form :tmp.measurement/quantity])]
    (into {} [[:measurement/ingredient tmp-ingredient]
              [:measurement/quantity (js/parseInt tmp-quantity)]
              (when tmp-unit
                [:measurement/unit tmp-unit])])))

(register-handler
  :add-new-measurement
  (fn [db [_ form]]
    (let [tmp-measurement (build-tmp-measurement db form)
          measurements (get-in db [:state :forms form :recipe/measurements] [])]
      (-> db
          (assoc-in [:state :forms form :show-new-measurement] false)
          (assoc-in [:state :forms form :tmp.measurement/ingredient] nil)
          (assoc-in [:state :forms form :tmp.measurement/unit] nil)
          (assoc-in [:state :forms form :tmp.measurement/quantity] nil)
          (assoc-in [:state :forms form :recipe/measurements] (conj measurements tmp-measurement))))))

(defn- collect-new-recipe-form [db form]
  (into {} [[:recipe/name (get-in db [:state :forms form :recipe/name])]
            (when-let [description (get-in db [:state :forms form :recipe/description])]
              [:recipe/description description])
            (when-let [portions (get-in db [:state :forms form :recipe/portions])]
              [:recipe/portions (js/parseInt portions)])
            [:recipe/preparation (get-in db [:state :forms form :recipe/preparation])]
            [:recipe/measurements (get-in db [:state :forms form :recipe/measurements])]]))

; create new recipe
(register-handler
  :create-recipe
  (fn [db [_ form]]
    (let [recipe (collect-new-recipe-form db form)]
      (println recipe)
      (POST (str js/context "/api/recipes")
            {:response-format :json
             :params          recipe
             :keywords?       true
             :handler         #(dispatch [:post-new-recipe %1])
             :error-handler   #(dispatch [:load-data-error %1])})
      (-> db
          (assoc-in [:state :forms form] {})
          (assoc-in [:state :loading] true)))))

;; load data error
(register-handler
  :post-new-recipe
  (fn [db [_ data]]
    (println "Result:" data)
    (assoc-in db [:state :loading] false)))