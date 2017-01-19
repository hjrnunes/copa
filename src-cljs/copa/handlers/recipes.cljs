(ns copa.handlers.recipes
  (:require [copa.db :refer [default-value app-schema]]
            [copa.ajax :refer [load-auth-interceptor!]]
            [re-frame.core :refer [reg-event-fx reg-event-db path trim-v after]]
            [day8.re-frame.http-fx]
            [plumbing.core :refer [map-vals]]
            [ajax.core :refer [json-request-format json-response-format]]
            [clojure.string :refer [lower-case]]))

;; get recipes
(reg-event-fx
  :get/recipes
  (fn [_ _]
    {:http-xhrio {:method          :get
                  :uri             (str js/context "/api/recipes")
                  :response-format (json-response-format {:keywords? true})
                  :on-success      [:response/get-recipes]
                  :on-failure      [:data/error]
                  }
     :dispatch   [:loading/start]}))

;; get recipes response
(reg-event-fx
  :response/get-recipes
  (fn [{:keys [db]} [_ data]]
    {:db         (-> db
                     (assoc-in [:data :recipes] data)
                     (assoc-in [:index :recipes] (map-vals first
                                                           (group-by :recipe_id data))))
     :dispatch-n [[:loading/stop]
                  [:get/ingredients]]}))

;; select recipe
(reg-event-db
  :recipe/select
  (fn [db [_ selected]]
    (-> db
        (assoc-in [:state :selected-recipe] selected)
        (assoc-in [:state :active-recipe-pane] :recipe-list))))

;; clear recipe

(defn- prep-recipe-form [db form]
  (-> form
      (assoc :user (get-in db [:state :user :username]))
      (assoc :measurements (for [{:keys [ingredient quantity unit]} (:measurements form)]
                             (into {} [[:ingredient (lower-case ingredient)]
                                       (when quantity
                                         [:quantity (js/parseFloat quantity)])
                                       (when unit
                                         [:unit unit])])))))

;; save recipe
(reg-event-fx
  :recipe/save
  (fn [{:keys [db]} [_ form]]
    (let [recipe (prep-recipe-form db form)]
      {:http-xhrio {:method          :post
                    :uri             (str js/context "/api/recipes")
                    :params          recipe
                    :format          (json-request-format)
                    :response-format (json-response-format {:keywords? true})
                    :on-success      [:response/recipe-save]
                    :on-failure      [:data/error]}
       :dispatch   [:loading/start]})))

;; recipe post result
(reg-event-fx
  :response/recipe-save
  (fn [{:keys [db]} [_ data]]
    (let [id (:recipe_id data)]
      {:db         (assoc-in db [:index :recipes id] data)
       :dispatch-n [[:loading/stop]
                    [:get/ingredients]
                    [:push-url-for :recipe :id id]]})))

;; delete recipe
(reg-event-fx
  :recipe/delete
  (fn [_ [_ id]]
    (let [params {:recipe_id id}]
      {:http-xhrio {:method          :delete
                    :uri             (str js/context "/api/recipes")
                    :params          params
                    :format          (json-request-format)
                    :response-format (json-response-format {:keywords? true})
                    :on-success      [:response/recipe-delete]
                    :on-failure      [:data/error]}
       :dispatch   [:loading/start]})))

;; recipe delete result
(reg-event-fx
  :response/recipe-delete
  (fn [{:keys [db]} [_ data]]
    (let [id (:recipe_id data)]
      {:db         (assoc-in db [:index :recipes] (dissoc (get-in db [:index :recipes]) id))
       :dispatch-n [[:loading/stop]
                    [:recipe/select nil]]})))