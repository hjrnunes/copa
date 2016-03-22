(ns copa.handlers
  (:require [copa.db :refer [default-value app-schema]]
            [re-frame.core :refer [register-handler dispatch path trim-v after]]
            [schema.core :as s]
            [plumbing.core :refer [map-vals]]
            [ajax.core :refer [GET POST]]))

;; -- Event Handlers ----------------------------------------------------------

;; load data request
(register-handler
  :data/load
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
  (fn [db [_ data]]
    (dispatch [:state/update :loading false])
    (-> db
        (assoc :recipes data)
        (assoc :index (map-vals first
                                (group-by :_id data))))))

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

;; form load for edit handler
(register-handler
  :form-state/load
  (fn [db [_ form data]]
    (assoc-in db [:state :forms form] data)))

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
    (into {} [[:ingredient tmp-ingredient]
              [:quantity (js/parseFloat tmp-quantity)]
              (when tmp-unit
                [:unit tmp-unit])])))

(register-handler
  :measurement/cancel
  (fn [db [_ form]]
    (dispatch [:form-state/update form :show-new-measurement false])
    (dispatch [:measurement/clear form])
    db))

(register-handler
  :measurement/clear
  (fn [db [_ form]]
    (-> db
        (assoc-in [:state :forms form :tmp.measurement/ingredient] nil)
        (assoc-in [:state :forms form :tmp.measurement/unit] nil)
        (assoc-in [:state :forms form :tmp.measurement/quantity] nil))))

;; add new measurement in recipe form
(register-handler
  :measurement/add
  (fn [db [_ form]]
    (let [tmp-measurement (build-tmp-measurement db form)
          measurements (get-in db [:state :forms form :measurements] [])]
      (dispatch [:measurement/clear form])
      (assoc-in db [:state :forms form :measurements] (conj measurements tmp-measurement)))))

;; delete measurement in recipe form
(register-handler
  :measurement/remove
  (fn [db [_ form measurement]]
    (let [measurements (get-in db [:state :forms form :measurements] [])]
      (assoc-in db [:state :forms form :measurements] (remove #{measurement} measurements)))))

;; clear recipe

(register-handler
  :recipe/clear
  (fn [db [_ form]]
    (assoc-in db [:state :forms form] {})))

;; create new recipe
(defn- collect-new-recipe-form [db form]
  (into {} [[:name (get-in db [:state :forms form :name])]
            (when-let [description (get-in db [:state :forms form :description])]
              [:description description])
            (when-let [portions (get-in db [:state :forms form :portions])]
              [:portions (js/parseInt portions)])
            [:preparation (get-in db [:state :forms form :preparation])]
            [:measurements (get-in db [:state :forms form :measurements])]]))

(register-handler
  :recipe/save
  (fn [db [_ form]]
    (let [recipe (collect-new-recipe-form db form)]
      (POST (str js/context "/api/recipes")
            {:response-format :json
             :params          recipe
             :keywords?       true
             :handler         #(dispatch [:response/recipe-save %1])
             :error-handler   #(dispatch [:data/error %1])})
      (dispatch [:state/update :loading true])
      (dispatch [:recipe/clear])
      db)))

;; recipe post result
(register-handler
  :response/recipe-save
  (fn [db [_ data]]
    (let [recp-id (:db/id data)]
      (dispatch [:state/update :loading false])
      (dispatch [:recipe/select recp-id])
      (-> db
          (update-in [:recipes] conj data)
          (assoc-in [:index recp-id] data)))))