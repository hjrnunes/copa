(ns copa.handlers.recipes
  (:require [copa.db :refer [default-value app-schema]]
            [copa.ajax :refer [load-auth-interceptor!]]
            [re-frame.core :refer [register-handler dispatch path trim-v after]]
            [plumbing.core :refer [map-vals]]
            [ajax.core :refer [GET POST DELETE]]
            [clojure.string :refer [lower-case]]))

;; get recipes
(register-handler
  :get/recipes
  (fn [db _]
    (GET (str js/context "/api/recipes")
         {:response-format :json
          :keywords?       true
          :handler         #(dispatch [:response/get-recipes %1])
          :error-handler   #(dispatch [:data/error %1])})
    (dispatch [:loading/start])
    db))

;; get recipes response
(register-handler
  :response/get-recipes
  (fn [db [_ data]]
    (dispatch [:loading/stop])
    (dispatch [:get/ingredients])
    (-> db
        (assoc-in [:data :recipes] data)
        (assoc-in [:index :recipes] (map-vals first
                                              (group-by :name data))))))

;; select recipe
(register-handler
  :recipe/select
  (fn [db [_ selected]]
    (-> db
        (assoc-in [:state :selected-recipe] selected)
        (assoc-in [:state :active-recipe-pane] :recipe-list))))

;; clear recipe

(defn- prep-recipe-form [db form]
  (-> form
      (dissoc :_id)
      (assoc :user (get-in db [:state :user :username]))
      (assoc :measurements (for [{:keys [ingredient quantity unit]} (:measurements form)]
                             (into {} [[:ingredient (lower-case ingredient)]
                                       (when quantity
                                         [:quantity (js/parseFloat quantity)])
                                       (when unit
                                         [:unit unit])])))))

;; save recipe
(register-handler
  :recipe/save
  (fn [db [_ form]]
    (let [recipe (prep-recipe-form db form)]
      (POST (str js/context "/api/recipes")
            {:response-format :json
             :params          recipe
             :keywords?       true
             :handler         #(dispatch [:response/recipe-save %1])
             :error-handler   #(dispatch [:data/error %1])})
      (dispatch [:loading/start])
      db)))

;; recipe post result
(register-handler
  :response/recipe-save
  (fn [db [_ data]]
    (let [name (:name data)
          db (-> db
                 (assoc-in [:index :recipes name] data))]
      (dispatch [:loading/stop])
      (dispatch [:recipe/select name])
      db)))

;; delete recipe
(register-handler
  :recipe/delete
  (fn [db [_ name]]
    (let [params {:name name}]
      (DELETE (str js/context "/api/recipes")
              {:response-format :json
               :params          params
               :keywords?       true
               :handler         #(dispatch [:response/recipe-delete %1])
               :error-handler   #(dispatch [:data/error %1])})
      (dispatch [:loading/start])
      db)))

;; recipe delete result
(register-handler
  :response/recipe-delete
  (fn [db [_ data]]
    (let [name (:name data)
          db (-> db
                 (assoc-in [:index :recipes] (dissoc (get-in db [:index :recipes]) name)))]
      (dispatch [:loading/stop])
      (dispatch [:recipe/select nil])
      db)))