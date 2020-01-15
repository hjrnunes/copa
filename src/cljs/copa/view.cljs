(ns copa.view
  (:require
    [kee-frame.core :as kf]
    [markdown.core :refer [md->html]]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [fork.core :as fork]
    ["@material-ui/core/CssBaseline" :default CssBaseline]
    ["@material-ui/core/AppBar" :default AppBar]
    ["@material-ui/core/Toolbar" :default Toolbar]
    ["@material-ui/core/IconButton" :default IconButton]
    ["@material-ui/core/Fab" :default Fab]
    ["@material-ui/core/Typography" :default Typography]
    ["@material-ui/core/Divider" :default Divider]

    ["@material-ui/core/Container" :default Container]

    ["@material-ui/core/List" :default List]

    ["@material-ui/core/ListItem" :default ListItem]
    ["@material-ui/core/ListItemText" :default ListItemText]
    ["@material-ui/core/ListItemSecondaryAction" :default ListItemSecondaryAction]

    ["@material-ui/core/Tabs" :default Tabs]
    ["@material-ui/core/Tab" :default Tab]

    ["@material-ui/core/TextField" :default TextField]
    ["@material-ui/core/TextareaAutosize" :default TextareaAutosize]

    ["@material-ui/core/Dialog" :default Dialog]
    ["@material-ui/core/DialogTitle" :default DialogTitle]
    ["@material-ui/core/DialogContent" :default DialogContent]
    ["@material-ui/core/DialogActions" :default DialogActions]

    ["@material-ui/icons/Menu" :default MenuIcon]
    ["@material-ui/icons/ArrowBack" :default ArrowBack]
    ["@material-ui/icons/Edit" :default Edit]
    ["@material-ui/icons/Done" :default Done]
    ["@material-ui/icons/Clear" :default Clear]
    ["@material-ui/icons/Add" :default Add]

    ["@material-ui/core/Fab" :default Fab]
    ))

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

(defn recipe-header [{:keys [recipe/name recipe/description]}]
  [:div {:style {:padding-top "1em"}}
   [:> Typography {:component "h2" :variant "h5"}
    name
    ]
   [:> Typography {:variant      "subtitle2"
                   :color        "textSecondary"
                   :gutterBottom true}
    description]])

(defn preparation-display
  [{:keys [recipe/preparation] :as recipe}]
  [:div {:style {:display            "grid"
                 :grid-template-rows "auto 1fr"
                 :height             "100%"}}
   [:div {:style {:grid-row 1}}
    [recipe-header recipe]
    [:> Divider]]
   [:div {:style {:grid-row 2}
          :dangerouslySetInnerHTML
                 {:__html (md->html preparation)}}]])

(defn preparation-form
  [{:keys [values
           form-id
           handle-change
           handle-blur
           touched
           submitting?
           handle-submit]}]
  [:div {:style {:display            "grid"
                 :grid-template-rows "auto 1fr"
                 :height             "100%"}}
   [:form
    {:id        form-id
     :on-submit handle-submit}
    [:div {:style {:grid-row 1}}
     [:div {:style {:padding-top "1em"}}
      [:> TextField {:name      "name"
                     :label     "name"
                     :fullWidth true
                     :margin    "dense"
                     :value     (values "name" "")
                     :onChange  handle-change
                     :onBlur    handle-blur}]
      [:> TextField {:name      "description"
                     :label     "description"
                     :fullWidth true
                     :margin    "dense"
                     :value     (values "description" "")
                     :onChange  handle-change
                     :onBlur    handle-blur}]]
     [:> Divider {:style {:margin-top "2em"}}]]
    [:div {:style {:grid-row 2}}
     [:> TextField {:name      "preparation"
                    :label     "preparation"
                    :fullWidth true
                    :margin    "normal"
                    :multiline true
                    :value     (values "preparation" "")
                    :rowsMax   js/Number.POSITIVE_INFINITY
                    :onChange  handle-change
                    :onBlur    handle-blur}]
     [:div
      {:style {:margin-top      "1em"
               :display         "flex"
               :justify-content "center"}}
      [:> Fab {:size       "medium"
               :color      "primary"
               :aria-label "Done"
               :type       "submit"
               :disabled   (or submitting? (empty? touched))
               :style      {:margin-right "0.2em"}}
       [:> Done]]
      [:> Fab {:size       "medium"
               :color      "secondary"
               :aria-label "Cancel"
               :onClick    #(rf/dispatch [:recipe/edit-cancel])}
       [:> Clear]]]]]
   ]
  )

(defn preparation-pane
  [{:keys [recipe/id recipe/name recipe/description recipe/preparation] :as recipe}]
  (let [editing? (rf/subscribe [:ui/editing?])]
    (if @editing?
      [fork/form {:path              :preparation-form
                  :form-id           "preparation"
                  :prevent-default?  true
                  :clean-on-unmount? true
                  :on-submit         #(rf/dispatch [:preparation/submit %])
                  :initial-values    {"id"          id
                                      "name"        name
                                      "description" description
                                      "preparation" preparation}}
       preparation-form]
      [preparation-display recipe]))
  )

(defn measurement-line
  [{:keys [measure/ingredient measure/quantity measure/unit]}]
  (let [editing? (rf/subscribe [:ui/editing?])]
    [:> ListItem {:dense   true
                  :divider true}
     [:> ListItemText
      [:> Typography {:variant "body1"}
       ingredient]]
     [:> ListItemSecondaryAction

      (if @editing?

        [:> IconButton
         {:color      "secondary"
          :aria-label "Cancel"
          :onClick    #(rf/dispatch [:recipe/edit-cancel])}
         [:> Clear]]

        [:> ListItemText
         [:> Typography {:variant "subtitle2"
                         :color   "textSecondary"}
          (str quantity " " unit)]])]
     ]))

(defn measurement-form
  [{:keys [values
           form-id
           handle-change
           handle-blur
           touched
           submitting?
           handle-submit]}]
  [:form
   {:id        form-id
    :on-submit handle-submit}
   [:div
    [:> DialogContent
     [:> TextField {:name      "quantity"
                    :label     "quantity"
                    :margin    "dense"
                    :fullWidth true
                    :onChange  handle-change
                    :onBlur    handle-blur}]
     [:> TextField {:name      "unit"
                    :label     "unit"
                    :margin    "dense"
                    :fullWidth true
                    :onChange  handle-change
                    :onBlur    handle-blur}]
     [:> TextField {:name      "ingredient"
                    :label     "ingredient"
                    :margin    "dense"
                    :fullWidth true
                    :onChange  handle-change
                    :onBlur    handle-blur}]]
    [:> DialogActions
     [:> IconButton
      {:color      "primary"
       :aria-label "Done"
       :type       "submit"
       :disabled   (or submitting? (empty? touched))}
      [:> Done]]
     [:> IconButton
      {:color      "secondary"
       :aria-label "Cancel"
       :onClick    #(rf/dispatch [:measurement/edit-cancel])}
      [:> Clear]]
     ]]]
  )

(defn add-measurement-dialog [recipe]
  (let [dialog? (rf/subscribe [:ui/measurement-dialog?])]
    [:> Dialog {:open @dialog?}
     ;[:> DialogTitle "Add measurement"]
     [fork/form {:path              :measurement-form
                 :form-id           "measurement"
                 :prevent-default?  true
                 :clean-on-unmount? true
                 :on-submit         #(rf/dispatch [:measurement/submit %])
                 :initial-values    {"recipe-id" (:recipe/id recipe)}}
      measurement-form]])
  )

(defn measurements-pane
  [{:keys [recipe/measurements] :as recipe}]
  (let [editing? (rf/subscribe [:ui/editing?])]
    [:div {:style {:display            "grid"
                   :grid-template-rows "auto 1fr"
                   :height             "100%"}}
     [:div {:style {:grid-row 1}}
      [recipe-header recipe]
      [:> List
       (for [measure measurements
             :let [{:keys [measure/id]} measure]]
         ^{:key id} [measurement-line measure])
       ]
      [:div {:style {:display         "flex"
                     :justify-content "flex-end"
                     :margin-top      "1em"
                     :padding-right   "1.2em"}}
       [:div
        [:> Fab {:size    "medium"
                 :color   "primary"
                 :onClick #(rf/dispatch [:measurement/edit])}
         [:> Add]]
        [add-measurement-dialog recipe]]
       (if @editing?
         [:> Fab {:style   {:margin-left "0.5em"}
                  :size    "medium"
                  :onClick #(rf/dispatch [:recipe/edit-cancel])}
          [:> Done]]
         )]
      ]]))


(defn recipe-view []
  (let [pane (r/atom :prep)
        recipe (rf/subscribe [:recipe/selected])
        editing? (rf/subscribe [:ui/editing?])]
    (fn []
      [:div {:style {:grid-row           2
                     :display            "grid"
                     :grid-template-rows "1fr auto"
                     :height             "100%"}}
       [:> Container {:style {:grid-row 1}}
        (case @pane
          :prep [preparation-pane @recipe]
          :ings [measurements-pane @recipe])]

       (when-not @editing?
         [:> Tabs {:value    @pane
                   :variant  "fullWidth"
                   :style    {:grid-row 2}
                   :onChange (fn [evt val] (reset! pane (keyword val)))}
          [:> Tab {:label "Preparação" :value :prep}]
          [:> Tab {:label "Ingredientes" :value :ings}]])])))


(defn app-bar []
  (let [show-back-btn? (rf/subscribe [:ui/show-back-btn?])
        show-edit-btn? (rf/subscribe [:ui/show-edit-btn?])]
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

        (when @show-back-btn?
          [:> IconButton
           {:edge       "start"
            :color      "inherit"
            :aria-label "Menu"
            :onClick    #(rf/dispatch [:nav/route-name :route/home])}
           [:> ArrowBack]])

        (when @show-edit-btn?
          [:> IconButton
           {:style      {:margin-left "auto"}
            :color      "inherit"
            :aria-label "Edit"
            :onClick    #(rf/dispatch [:recipe/edit])}
           [:> Edit]])
        ]
       ])))

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
