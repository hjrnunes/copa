(ns copa.lang.en)

(def lang
  {:core/login-header                  "COPA - identification"
   :core/login-expired                 "Session expired!"
   :core/login-user-ph                 "user"
   :core/login-pass-ph                 "password"
   :core/login-button-label            "OK"

   :core/menu-item-recipes             "Recipes"
   :core/menu-item-ingredients         "Ingredients"

   :recipes/menu-search-ph             "Find recipe..."
   :recipes/menu-add                   "Add recipe"
   :recipes/menu-edit                  "Edit recipe"
   :recipes/menu-delete                "Delete recipe"

   :recipes/details-portions-s         "portion"
   :recipes/details-portions-p         "portions"
   :recipes/details-name               "Name"
   :recipes/details-description        "Description"
   :recipes/details-preparation        "Preparation"
   :recipes/details-ingredients        "Ingredients"
   :recipes/details-duration           "Duration"
   :recipes/details-source             "Source"

   :recipes/breadcrumb-recipes         "Recipes"
   :recipes/breadcrumb-new-recipe      "New Recipe"

   :recipes/edit-button-label-save     "Save"
   :recipes/edit-button-label-or       "or"
   :recipes/edit-button-label-cancel   "Cancel"

   :recipes/edit-md-tt                 "This field accepts Markdown syntax. See the link for details."
   :recipes/edit-md-link               "Styling with Markdown is supported"

   :ingredients/is-in-recipes          "Goes in the following recipes:"

   :user/recipes                       "Recipes"

   :user/preferences                   "Preferences"
   :user/language                      "Language"

   :user/update-pass                   "Update password"
   :user/update-pass-current-ph        "Current password"
   :user/update-pass-new-ph            "New password"
   :user/update-pass-confirm-ph        "Confirm new password"

   :admin/state                        "State"
   :admin/new-user-heading             "New user"
   :admin/new-user-user-label          "User"
   :admin/new-user-user-ph             "username"
   :admin/new-user-password-label      "Password"
   :admin/new-user-password-ph         "password"
   :admin/new-user-admin-label         "Admin"
   :admin/new-user-button-label-save   "Save"
   :admin/new-user-button-label-or     "or"
   :admin/new-user-button-label-cancel "Cancel"

   :admin/menu-add                     "Add user"
   :admin/menu-edit                    "Edit user"
   :admin/menu-delete                  "Delete user"
   })
