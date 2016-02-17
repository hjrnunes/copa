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
  :data/load
  check-schema-mw
  (fn [db _]
    (GET (str js/context "/api/recipes")
         {:response-format :json
          :keywords?       true
          :handler         #(dispatch [:data/response %1])
          :error-handler   #(dispatch [:data/error %1])})
    (dispatch [:state/update :loading true])
    db))

;; load data response
(register-handler
  :data/response
  check-schema-mw
  (fn [db [_ data]]
    (dispatch [:state/update :loading false])
    (-> db
        (assoc :recipes data)
        (assoc :index (map-vals first
                                (group-by :db/id data))))))

;; load data error
(register-handler
  :data/error
  (fn [db [_ data]]
    (println "Error:" data)
    (dispatch [:state/update :loading false])
    db))

;; generic update state handler
(register-handler
  :state/update
  (fn [db [_ key value]]
    (assoc-in db [:state key] value)))

;; generic update form state handler
(register-handler
  :form-state/update
  (fn [db [_ form key value]]
    (assoc-in db [:state :forms form key] value)))

;; select recipe
(register-handler
  :recipe/select
  (fn [db [_ selected]]
    (-> db
        (assoc-in [:state :selected-recipe] selected)
        (assoc-in [:state :active-pane] :recipe-details))))

;; update new recipe measurements list
(defn- build-tmp-measurement [db form]
  (let [tmp-ingredient (get-in db [:state :forms form :tmp.measurement/ingredient])
        tmp-unit (get-in db [:state :forms form :tmp.measurement/unit])
        tmp-quantity (get-in db [:state :forms form :tmp.measurement/quantity])]
    (into {} [[:measurement/ingredient tmp-ingredient]
              [:measurement/quantity (js/parseFloat tmp-quantity)]
              (when tmp-unit
                [:measurement/unit tmp-unit])])))

;; add new measurement in new recipe form
(register-handler
  :measurement/add
  (fn [db [_ form]]
    (let [tmp-measurement (build-tmp-measurement db form)
          measurements (get-in db [:state :forms form :recipe/measurements] [])]
      (dispatch [:form-state/update form :show-new-measurement false])
      (-> db
          (assoc-in [:state :forms form :tmp.measurement/ingredient] nil)
          (assoc-in [:state :forms form :tmp.measurement/unit] nil)
          (assoc-in [:state :forms form :tmp.measurement/quantity] nil)
          (assoc-in [:state :forms form :recipe/measurements] (conj measurements tmp-measurement))))))

;; create new recipe
(defn- collect-new-recipe-form [db form]
  (into {} [[:recipe/name (get-in db [:state :forms form :recipe/name])]
            (when-let [description (get-in db [:state :forms form :recipe/description])]
              [:recipe/description description])
            (when-let [portions (get-in db [:state :forms form :recipe/portions])]
              [:recipe/portions (js/parseInt portions)])
            [:recipe/preparation (get-in db [:state :forms form :recipe/preparation])]
            [:recipe/measurements (get-in db [:state :forms form :recipe/measurements])]]))

(register-handler
  :recipe/create
  (fn [db [_ form]]
    (let [recipe (collect-new-recipe-form db form)]
      (println recipe)
      (POST (str js/context "/api/recipes")
            {:response-format :json
             :params          recipe
             :keywords?       true
             :handler         #(dispatch [:recipe/post %1])
             :error-handler   #(dispatch [:data/error %1])})
      (dispatch [:state/update :loading false])
      (-> db
          (assoc-in [:state :forms form] {})))))

;; recipe post result
(register-handler
  :recipe/post
  (fn [db [_ data]]
    (let [recp-id (:db/id data)]
      (dispatch [:state/update :loading false])
      (dispatch [:recipe/select recp-id])
      (-> db
          (update-in [:recipes] conj data)
          (assoc-in [:index recp-id] data)))))