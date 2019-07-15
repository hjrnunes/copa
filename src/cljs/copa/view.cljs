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

    ["@material-ui/core/Tabs" :default Tabs]
    ["@material-ui/core/Tab" :default Tab]

    ["@material-ui/core/BottomNavigation" :default BottomNavigation]
    ["@material-ui/core/BottomNavigationAction" :default BottomNavigationAction]

    ["@material-ui/icons/Menu" :default MenuIcon]

    ["@material-ui/styles" :refer (makeStyles)]))


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

(def txt "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")

(defn recipe-view []
  (let []
    (fn []
      [:div {:style {:grid-row           2
                     :display            "grid"
                     :grid-template-rows "auto 1fr"
                     :height             "100%"}}
       [:> Tabs {:value   0
                 :variant "fullWidth"
                 :style   {:grid-row 1}
                 }
        [:> Tab {:label "A"}
         ]
        [:> Tab {:label "B"}]]
       [:> Container {:style {:grid-row 2}}
        [:> Typography {:variant "body1"}
         txt]]
       ])))

(defn app-bar []
  (let []
    (fn []
      [:> AppBar
       {:style    {:grid-row 1}
        :position "relative"}
       [:> Toolbar
        [:> IconButton
         {:edge       "start"
          :color      "inherit"
          :aria-label "Menu"}
         [:> MenuIcon]]]
       ])))

(defn home-page []
  [:div {:style {:height "100%"}}
   [:> CssBaseline]

   [:div {:style {
                  :display            "grid"
                  :grid-template-rows "auto 1fr"
                  :height             "100%"}}

    [app-bar]

    ;[recipe-view]

    [:div {:style {:grid-row           2
                   ;:display            "grid"
                   ;:grid-template-rows "auto 1fr"
                   :height             "100%"}}
      [:> List]
     ]

    ]])


(defn root-component []
  [:div {:style {:height "100%"}}
   ;[navbar]
   [kf/switch-route (fn [route] (get-in route [:data :name]))
    :home home-page
    :about about-page
    nil [:div ""]]])
