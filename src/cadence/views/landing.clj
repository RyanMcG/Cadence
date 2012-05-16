(ns cadence.views.landing
  (:require [cadence.views.common :as common]
            [cadence.model :as model]
            [cadence.model.flash :as flash]
            [cemerick.friend :as friend]
            [noir.response :as resp])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpage root "/" []
  (common/layout
    [:div.page-header [:h1 "Cadence"]]
    [:p "Welcome to cadence"]))

(defpage login "/login" []
  (common/layout
    [:div.page-header [:h1 "Login"]]
    (common/default-form
      :#login.well.form-inline
      {:action "/login" :method "POST"}
      [{:type "username" :name "Username"}
       {:type "password" :name "Password"}]
      [{:value "Log In"}])))

(defn- clear-identity [response]
  (update-in response [:session] dissoc ::identity))

(defpage logout "/logout" []
  (flash/put! :success "You have been logged out.")
  (clear-identity (resp/redirect "/")))

(defpage signup "/signup" []
  (common/layout
    [:div.page-header [:h1 "Sign Up"]]
    (common/control-group-form
      :#login.well.form-horizontal
      {:action "/signup" :method "POST"}
      [{:type "username" :name "Username"}
       {:type "text" :name "Name" :placeholder "Optional"}
       {:type "text" :name "Email" :placeholder "Optional"}
       {:type "password" :name "Password"}
       {:type "password" :name "Repeat Password"}]
      [{:eclass :.btn-primary
        :value "Sign Up"}])))

(defpage signup-check [:post "/signup"] {:as user}
  (if (model/add-user user)
    (do
      (flash/put! :success "Successfully Signed Up!")
      (resp/redirect (url-for root)))
    (do
      (flash/put! :error "Sign Up Failed.")
      (resp/redirect (url-for signup)))))

(defpage about "/about" []
  (common/layout
    [:div.page-header [:h1 "About"]]
    [:p "derpderpderp"]))
