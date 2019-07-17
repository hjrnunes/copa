(ns copa.core
  (:require
    [kee-frame.core :as kf]
    [re-frame.core :as rf]
    [ajax.core :as http]
    [copa.ajax :as ajax]
    [copa.routing :as routing]
    [copa.view :as view]))

;; Recipes by id
(rf/reg-sub
  :recipe/by-id
  (fn [db _]
    (:recipe/by-id db)))

;; Recipes sequence
(rf/reg-sub
  :data/recipes
  :<- [:recipe/by-id]
  (fn [by-id _]
    (vals by-id)))

;; Recipe names
(rf/reg-sub
  :recipe/names
  :<- [:data/recipes]
  (fn [recipes _]
    (map #(select-keys % [:recipe/id :recipe/name]) recipes)))

;; Selected recipe id
(rf/reg-sub
  :recipe/selected-id
  (fn [db _]
    (:recipe/selected db)))

;; Selected recipe
(rf/reg-sub
  :recipe/selected
  :<- [:recipe/by-id]
  :<- [:recipe/selected-id]
  (fn [[recipes id] _]
    (get recipes id)))

;; show back button?
(rf/reg-sub
  :ui/show-back-btn?
  :<- [:route/name]
  (fn [route-name _]
    (some #{:route/recipe} [route-name])))

;(rf/reg-sub
;  :view/dropdowns
;  (fn [db _]
;    (:view/dropdowns db)))
;
;(rf/reg-sub
;  :view/dropdown
;  :<- [:view/dropdowns]
;  (fn [dds [_ ddown]]
;    (get dds ddown)))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(rf/reg-event-fx
  ::load-about-page
  (constantly nil))

(kf/reg-controller
  ::about-controller
  {:params (constantly true)
   :start  [::load-about-page]})

(rf/reg-sub
  :docs
  (fn [db _]
    (:docs db)))

(kf/reg-chain
  ::load-home-page
  (fn [_ _]
    {:http-xhrio {:method          :get
                  :uri             "/docs"
                  :response-format (http/raw-response-format)
                  :on-failure      [:common/set-error]}})
  (fn [{:keys [db]} [_ docs]]
    {:db (assoc db :docs docs)}))

(kf/reg-controller
  ::home-controller
  {:params (constantly true)
   :start  [::load-home-page]})

(kf/reg-event-db
  :recipe/select
  (fn [db [id]]
    (assoc db :recipe/selected id)))

(kf/reg-controller
  ::recipe-controller
  {:params (fn [{:keys [data path-params]}]
             (when (-> data :name (= :route/recipe))
               path-params))
   :start  (fn [_ params]
             [:recipe/select (:id params)])})

;; -------------------------
;; Initialize app

(def recipes {"1" #:recipe{:id           1,
                           :name         "Penne alla senese",
                           :description  "Penne com salsicha, nozes e natas",
                           :preparation  "*notas ingredientes*\n\n- *salsichas*: desfeitas sem pele\n- *nozes*: sem casca, picadas finas",
                           :user         "admin",
                           :measurements [#:measure{:id         1,
                                                    :quantity   20.0,
                                                    :unit       "g",
                                                    :ingredient "manteiga"
                                                    },
                                          #:measure{:id         2,
                                                    :quantity   1.0,
                                                    :unit       "colheres",
                                                    :ingredient "café"}]}
              "2" #:recipe{:id           2,
                           :name         "Penne alla senese 2",
                           :description  "Penne com salsicha, nozes e natas",
                           :preparation  "*notas ingredientes*\n\n- *salsichas*: desfeitas sem pele\n- *nozes*: sem casca, picadas finas",
                           :user         "admin",
                           :measurements [#:measure{:id         1,
                                                    :quantity   20.0,
                                                    :unit       "g",
                                                    :ingredient "manteiga"
                                                    },
                                          #:measure{:id         2,
                                                    :quantity   1.0,
                                                    :unit       "colheres",
                                                    :ingredient "café"}]}})

(def initial-db {:recipe/by-id recipes})

(defn ^:dev/after-load mount-components
  ([] (mount-components true))
  ([debug?]
   (rf/clear-subscription-cache!)
   (kf/start! {:debug?         (boolean debug?)
               :routes         routing/routes
               :hash-routing?  true
               :initial-db     initial-db
               :root-component [view/root-component]})))

(defn init! [debug?]
  (ajax/load-interceptors!)
  (mount-components debug?))
