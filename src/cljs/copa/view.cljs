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

    ["@material-ui/core/Tabs" :default Tabs]
    ["@material-ui/core/Tab" :default Tab]

    ["@material-ui/core/BottomNavigation" :default BottomNavigation]
    ["@material-ui/core/BottomNavigationAction" :default BottomNavigationAction]

    ["@material-ui/icons/Menu" :default MenuIcon]

    ["@material-ui/styles" :refer (makeStyles)]))

;import CssBaseline from '@material-ui/core/CssBaseline';
;import Drawer from '@material-ui/core/Drawer';
;import AppBar from '@material-ui/core/AppBar';
;import Toolbar from '@material-ui/core/Toolbar';
;import List from '@material-ui/core/List';
;import Typography from '@material-ui/core/Typography';
;import Divider from '@material-ui/core/Divider';
;import IconButton from '@material-ui/core/IconButton';
;import Badge from '@material-ui/core/Badge';
;import Container from '@material-ui/core/Container';
;import Grid from '@material-ui/core/Grid';
;import Paper from '@material-ui/core/Paper';
;import Link from '@material-ui/core/Link';
;import MenuIcon from '@material-ui/icons/Menu';
;import ChevronLeftIcon from '@material-ui/icons/ChevronLeft';
;import NotificationsIcon from '@material-ui/icons/Notifications';

;import BottomNavigation from '@material-ui/core/BottomNavigation';
;import BottomNavigationAction from '@material-ui/core/BottomNavigationAction';

; import { makeStyles } from '@material-ui/styles';

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

(defn home-page []
  [:> CssBaseline
   [:> AppBar
    [:> Toolbar
     [:> IconButton
      {:edge       "start"
       :color      "inherit"
       :aria-label "Menu"}
      [:> MenuIcon]]]]

   [:> Container
    [:> Tabs
     [:> Tab {:label "A"}]
     [:> Tab {:label "B"}]]
    ]

   ])


(defn root-component []
  [:div
   ;[navbar]
   [kf/switch-route (fn [route] (get-in route [:data :name]))
    :home home-page
    :about about-page
    nil [:div ""]]])
