(ns cadence.views.landing
  (:require [cadence.views.common :as common]
            [cadence.model :as model]
            (noir [response :as resp]
                  [session :as sess]))
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpage root "/" []
  (common/layout
    [:div.page-header [:h1 "Cadence"]]
    [:p "Welcome to cadence"]))

(defpage login-page "/login" []
  (common/layout
    [:div.page-header [:h1 "Login"]]
    (common/default-form
      :#login.well.form-inline
      {:action "/login" :method "POST"}
      [{:type "username" :name "Username"}
       {:type "password" :name "Password"}]
      [{:value "Log In"}])))

;(defpage login-check [:post "/login"] {:as user}
  ;(if (model/get-user (:username user))
    ;(do
      ;(sess/flash-put! "Successfully Logged In!")
      ;(resp/redirect (url-for root)))
    ;(do
      ;(sess/flash-put! "Log In Failed.")
      ;(resp/redirect (url-for login)))))

(defpage logout "/logout" []
  (sess/clear!)
  (sess/flash-put! "You have been logged out.")
  (resp/redirect "/"))

(defpage signup "/signup" []
  (common/layout
    [:div.page-header [:h1 "Sign Up"]]
    (common/control-group-form
      :#login.well.form-horizontal
      {:action "/signup" :method "POST"}
      [{:type "username" :name "Username"}
       {:type "text" :name "Name" :placeholder "Optional"}
       {:type "text" :name "Email" :placeholder "Optional"}
       {:type "password" :name "Password"}]
      [{:eclass :.btn-primary
        :value "Sign Up"}])))

(defpage signup-check [:post "/signup"] {:as user}
  (if (model/add-user user)
    (do
      (sess/flash-put! "Successfully Signed Up!")
      (resp/redirect (url-for root)))
    (do
      (sess/flash-put! "Sign Up Failed.")
      (resp/redirect (url-for signup)))))

(defpage about "/about" []
  (common/layout
    [:div.page-header [:h1 "About"]]
    [:p "derpderpderp"]))
