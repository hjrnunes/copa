(ns copa.core
  (:require
    [kee-frame.core :as kf]
    [re-frame.core :as rf]
    [ajax.core :as http]
    [copa.ajax :as ajax]
    [copa.routing :as routing]
    [copa.view :as view]
    [copa.db :as db]
    [copa.ws :as ws]
    [copa.sync :as sync]
    [fork.core :as fork]
    [re-posh.core :as rp]
    )
  )

(def fsm {:list                {:select-recipe :preparation}
          :preparation         {:back             :list
                                :show-ingredients :ingredients
                                :edit             :editing-preparation}
          :ingredients         {:back :list
                                :edit :editing-ingredients
                                :add  :ingredient-modal}
          :editing-ingredients {:delete-ingredient :editing-ingredients
                                :edit-ingredient   :ingredient-modal
                                :done              :ingredients}
          ;:ingredient-modal    {}
          ;:editing-preparation {}

          })

;; Recipe names
(rp/reg-sub
  :recipe/ids
  (fn [_ _]
    {:type  :query
     :query '[:find [?id ...]
              :where [?id :recipe/id _]]}))

(rp/reg-sub
  :recipe/names
  :<- [:recipe/ids]
  (fn [recipe-ids _]
    {:type    :pull-many
     :pattern '[:recipe/id :recipe/name]
     :ids     recipe-ids}))
;;;;;;;;;;;;;;;;

;; Selected recipe id
(rf/reg-sub
  :recipe/selected-id
  (fn [db _]
    (:recipe/selected db)))

(rp/reg-sub
  :recipe/selected
  :<- [:recipe/selected-id]
  (fn [recipe-id _]
    {:type    :pull
     :pattern '[*]
     :id      [:recipe/id recipe-id]}))

;;;;;;; editing

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

(defn to-recipe-ent [{:strs [id name description preparation]}]
  {:db/id              [:recipe/id id]
   :recipe/name        name
   :recipe/description description
   :recipe/preparation preparation})

(rf/reg-event-fx
  :preparation/submit
  [(rp/inject-cofx :ds)
   (fork/on-submit :preparation-form)]
  (fn [{:keys [ds db]} [_ {:keys [values]}]]
    {
     :transact [(to-recipe-ent values)]
     :db       (-> (fork/set-submitting db :preparation-form false)
                   (assoc :ui/editing? false))
     }))

;;;;;;; measurement dialog

(rf/reg-sub
  :ui/measurement-dialog?
  (fn [db _]
    (:ui/measurement-dialog? db false)))

(kf/reg-event-db
  :measurement/edit
  (fn [db _]
    (-> db
        (assoc :ui/measurement-dialog? true)
        (assoc :ui/editing? true))))

(kf/reg-event-db
  :measurement/edit-cancel
  (fn [db _]
    (-> db
        (assoc :ui/measurement-dialog? false)
        (assoc :ui/editing? false))))


(defn to-measurement-ent [{:strs [recipe-id ingredient quantity unit]}]
  {:db/id               [:recipe/id recipe-id]
   :recipe/measurements [(cond-> {}
                                 ingredient
                                 (assoc :measure/ingredient ingredient)
                                 quantity
                                 (assoc :measure/quantity quantity)
                                 unit
                                 (assoc :measure/unit unit))]})

(rf/reg-event-fx
  :measurement/submit
  [(rp/inject-cofx :ds)
   (fork/on-submit :measurement-form)]
  (fn [{:keys [ds db]} [_ {:keys [values]}]]
    {:transact [(to-measurement-ent values)]
     :db       (-> (fork/set-submitting db :preparation-form false)
                   (assoc :ui/editing? false)
                   (assoc :ui/measurement-dialog? false))}))

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

(defn ^:dev/after-load mount-components
  ([] (mount-components true))
  ([debug?]
   (rf/clear-subscription-cache!)
   (kf/start! {:debug?         (boolean debug?)
               :routes         routing/routes
               :hash-routing?  true
               :initial-db     {:ui/editing? false}
               :root-component [view/root-component]})
   (ws/start-router!)
   ))

(defn init! [debug?]
  (ajax/load-interceptors!)
  (mount-components debug?))
