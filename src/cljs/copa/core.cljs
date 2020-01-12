(ns copa.core
  (:require
    [kee-frame.core :as kf]
    [re-frame.core :as rf]
    [ajax.core :as http]
    [copa.ajax :as ajax]
    [copa.routing :as routing]
    [copa.view :as view]
    [fork.core :as fork]
    [compound2.core :as c]))

(rf/reg-sub
  :recipes
  (fn [db _]
    (:recipes db)))

;; Recipes by id
(rf/reg-sub
  :recipe/by-id
  :<- [:recipes]
  (fn [recipes _]
    (:recipe/by-id recipes)))

;; Recipes sequence
(rf/reg-sub
  :data/recipes
  :<- [:recipes]
  (fn [by-id _]
    (c/items by-id)))

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

;; Selected recipe id
(rf/reg-sub
  :ui/editing?
  (fn [db _]
    (:ui/editing? db)))

;; show back button?
(rf/reg-sub
  :ui/show-back-btn?
  :<- [:route/name]
  :<- [:ui/editing?]
  (fn [[route-name editing?] _]
    (and (some #{:route/recipe} [route-name])
         (not editing?))))

;; show edit button?
(rf/reg-sub
  :ui/show-edit-btn?
  :<- [:ui/show-back-btn?]
  :<- [:ui/editing?]
  (fn [[show-back-btn? editing?] _]
    (and show-back-btn?
         (not editing?))))

(kf/reg-event-db
  :recipe/edit
  (fn [db _]
    (assoc db :ui/editing? true)))

(kf/reg-event-db
  :recipe/edit-cancel
  (fn [db _]
    (assoc db :ui/editing? false)))

(defn to-recipe [m]
  (into {}
        (for [[k v] m]
          [(keyword "recipe" k) v])))

(rf/reg-event-fx
  :recipe/submit
  [(fork/on-submit :recipe-form)]
  (fn [{db :db} [_ {:keys [values]}]]
    {:db (-> (fork/set-submitting db :form false)
             (assoc :ui/editing? false)
             (assoc :recipes (c/add-items
                               (:recipes db)
                               [(to-recipe values)])))}))

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

(def initial-db {:recipes     (-> (c/compound [{:id          :recipe/by-id
                                                :kfn         :recipe/id
                                                :on-conflict (fn [a b] (merge a b))}])
                                  (c/add-items [#:recipe{:id           "1",
                                                         :name         "Penne alla senese",
                                                         :description  "Penne com salsicha, nozes e natas",
                                                         :preparation  "*notas ingredientes*\n\n- *salsichas*: desfeitas sem pele\n- *nozes*: sem casca, picadas finas",
                                                         :user         "admin",
                                                         :measurements [#:measure{:id         "1",
                                                                                  :quantity   20.0,
                                                                                  :unit       "g",
                                                                                  :ingredient "manteiga"
                                                                                  },
                                                                        #:measure{:id         "2",
                                                                                  :quantity   1.0,
                                                                                  :unit       "colheres",
                                                                                  :ingredient "café"}
                                                                        #:measure{:id         "3",
                                                                                  :quantity   1.0,
                                                                                  :ingredient "marmelo"}
                                                                        #:measure{:id         "4",
                                                                                  :ingredient "laranja"}]}
                                                #:recipe{:id           "2",
                                                         :name         "Penne alla senese 2",
                                                         :description  "Penne com salsicha, nozes e natas",
                                                         :preparation  "*notas ingredientes*\n\n- *salsichas*: desfeitas sem pele\n- *nozes*: sem casca, picadas finas",
                                                         :user         "admin",
                                                         :measurements [#:measure{:id         "1",
                                                                                  :quantity   20.0,
                                                                                  :unit       "g",
                                                                                  :ingredient "manteiga"
                                                                                  },
                                                                        #:measure{:id         "2",
                                                                                  :quantity   1.0,
                                                                                  :unit       "colheres",
                                                                                  :ingredient "café"}]}
                                                ]))
                 :ui/editing? false})

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
