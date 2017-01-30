(ns copa.handlers.ingredients
  (:require [re-frame.core :refer [reg-event-fx reg-event-db]]
            [day8.re-frame.http-fx]
            [plumbing.core :refer [map-vals]]
            [ajax.core :refer [json-request-format json-response-format]]
            [copa.util :refer [common-interceptors]]))

;; get ingredients
(reg-event-fx
  :get/ingredients
  common-interceptors
  (fn [_ _]
    {:http-xhrio {:method          :get
                  :uri             (str js/context "/api/ingredients")
                  :response-format (json-response-format {:keywords? true}) ;; IMPORTANT!: You must provide this.
                  :on-success      [:response/get-ingredients]
                  :on-failure      [:data/error]}
     :dispatch   [:loading/start]}))

;; get ingredients response
(reg-event-fx
  :response/get-ingredients
  common-interceptors
  (fn [{:keys [db]} [data]]
    {:db       (-> db
                   (assoc-in [:data :ingredients] data)
                   (assoc-in [:index :ingredients] (map-vals first
                                                             (group-by :ingredient_id data))))
     :dispatch [:loading/stop]}))

;; match selected ingredients
(reg-event-fx
  :ingredients/match
  common-interceptors
  (fn [{:keys [db]} _]
    (let [selected (get-in db [:state :selected-ingredients-ids])
          ingredients (vec (for [id (seq selected)
                                 :let [ingredient (get-in db [:index :ingredients id])]]
                             (:name ingredient)))
          params {:ingredients ingredients}]
      {:http-xhrio {:method          :post
                    :uri             (str js/context "/api/match")
                    :params          params
                    :format          (json-request-format)
                    :response-format (json-response-format {:keywords? true})
                    :on-success      [:response/ingredients-match]
                    :on-failure      [:data/error]}
       :dispatch   [:loading/start]})))

;; match post result
(reg-event-fx
  :response/ingredients-match
  common-interceptors
  (fn [{:keys [db]} [data]]
    (print "MATCH RESP:" data)
    {:db         (assoc-in db [:state :matched-recipes-ids] (map :recipe_id data))
     :dispatch-n [[:loading/stop]]}))

(defn- select-ingredient [db selected]
  (update-in db [:state :selected-ingredients-ids] conj selected))

(defn- deselect-ingredient [db deselected]
  (update-in db [:state :selected-ingredients-ids] #(remove #{deselected} %)))

;; select ingredient and match immediately
(reg-event-fx
  :ingredient/select-match
  common-interceptors
  (fn [{:keys [db]} [selected]]
    (print "SEL MATCH: " select-ingredient)
    {:db       (select-ingredient db selected)
     :dispatch [:ingredients/match]}))

;; select ingredient and match immediately
(reg-event-fx
  :ingredient/deselect-match
  common-interceptors
  (fn [{:keys [db]} [deselected]]
    {:db       (deselect-ingredient db deselected)
     :dispatch [:ingredients/match]}))

;; select ingredient
(reg-event-db
  :ingredient/select
  common-interceptors
  (fn [db [selected]]
    (select-ingredient db selected)))

;; deselect ingredient
(reg-event-db
  :ingredient/deselect
  common-interceptors
  (fn [db [_ deselected]]
    (deselect-ingredient db deselected)))