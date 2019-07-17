(ns copa.view
  (:require
    [kee-frame.core :as kf]
    [markdown.core :refer [md->html]]
    [reagent.core :as r]
    [re-frame.core :as rf]
    ["@material-ui/core/CssBaseline" :default CssBaseline]
    ["@material-ui/core/AppBar" :default AppBar]
    ["@material-ui/core/Toolbar" :default Toolbar]
    ["@material-ui/core/IconButton" :default IconButton]
    ["@material-ui/core/Typography" :default Typography]

    ["@material-ui/core/Container" :default Container]

    ["@material-ui/core/List" :default List]

    ["@material-ui/core/ListItem" :default ListItem]
    ["@material-ui/core/ListItemText" :default ListItemText]

    ["@material-ui/core/Tabs" :default Tabs]
    ["@material-ui/core/Tab" :default Tab]

    ["@material-ui/icons/Menu" :default MenuIcon]
    ["@material-ui/icons/ArrowBack" :default ArrowBack]
    ))


(defn nav-link [title page]
  [:a.navbar-item
   {:href  (kf/path-for [page])
    :class (when (= page @(rf/subscribe [:nav/page])) "is-active")}
   title])

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
    [:nav.navbar.is-info>div.container
     [:div.navbar-brand
      [:a.navbar-item {:href "/" :style {:font-weight :bold}} "copa"]
      [:span.navbar-burger.burger
       {:data-target :nav-menu
        :on-click    #(swap! expanded? not)
        :class       (when @expanded? :is-active)}
       [:span] [:span] [:span]]]
     [:div#nav-menu.navbar-menu
      {:class (when @expanded? :is-active)}
      [:div.navbar-start
       [nav-link "Home" :home]
       [nav-link "About" :about]]]]))

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]])

;(defn home-page []
;  [:section.section>div.container>div.content
;   (when-let [docs @(rf/subscribe [:docs])]
;     [:div {:dangerouslySetInnerHTML {:__html (md->html docs)}}])])


(defn list-item [{:keys [recipe/id recipe/name]}]
  [:> ListItem {:button  true
                :key     id
                :onClick #(rf/dispatch [:nav/route-name-params :route/recipe {:id id}])}
   [:> ListItemText {:primary name}]])

(defn recipe-list []
  (let [recipes (rf/subscribe [:recipe/names])]
    (fn []
      [:> List {:component "nav"}
       (for [recipe @recipes
             :let [{:keys [recipe/id]} recipe]]
         ^{:key id} [list-item recipe])])))

(defn recipe-view []
  (let [pane (r/atom :prep)
        recipe (rf/subscribe [:recipe/selected])]
    (fn []
      (let [{:keys [recipe/name recipe/preparation]} @recipe]
        [:div {:style {:grid-row           2
                       :display            "grid"
                       :grid-template-rows "auto 1fr"
                       :height             "100%"}}
         [:> Tabs {:value    @pane
                   :variant  "fullWidth"
                   :style    {:grid-row 1}
                   :onChange (fn [evt val] (reset! pane (keyword val)))}
          [:> Tab {:label "Preparação" :value :prep}]
          [:> Tab {:label "Ingredientes" :value :ings}]]

         [:> Container {:style {:grid-row 2}}
          (case @pane
            :prep [:div
                   [:> Typography {:variant "h4"}
                    name]
                   [:> Typography {:variant "body1"}
                    preparation]]
            :ings [:div
                   [:> Typography {:variant "h4"}
                    "Ingredientes"]
                   [:> Typography {:variant "body1"}
                    preparation]])]]))))

(defn app-bar []
  (let [show-back? (rf/subscribe [:ui/show-back-btn?])]
    (fn []
      [:> AppBar
       {:style    {:grid-row 1}
        :position "relative"}
       [:> Toolbar
        [:> IconButton
         {:edge       "start"
          :color      "inherit"
          :aria-label "Menu"}
         [:> MenuIcon]]
        (when @show-back?
          [:> IconButton
           {:edge       "start"
            :color      "inherit"
            :aria-label "Menu"
            :onClick    #(rf/dispatch [:nav/route-name :route/home])}
           [:> ArrowBack]])]
       ])))

;(defn root-component []
;  [:div {:style {:height "100%"}}
;   ;[navbar]
;   [kf/switch-route (fn [route] (get-in route [:data :name]))
;    :route/home home-page
;    :route/recipe
;    nil [:div ""]]])


(defn root-component []
  [:div {:style {:height "100%"}}
   [:> CssBaseline]
   [:div {:style {
                  :display            "grid"
                  :grid-template-rows "auto 1fr"
                  :height             "100%"}}
    [app-bar]

    [kf/switch-route (fn [route] (get-in route [:data :name]))
     :route/home recipe-list
     :route/recipe recipe-view
     nil [:div ""]]
    ]])
