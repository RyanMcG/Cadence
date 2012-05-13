(ns cadence.views.landing
  (:require [cadence.views.common :as common]
            (noir [response :as resp]
                  [session :as sess]))
  (:use noir.core
        [hiccup.core :exclude [as-str]]
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
      [{:type "email" :name "E-mail"}
       {:type "password" :name "Password"}]
      [{:value "Log In"}])))

(defpage login-check [:post "/login"] []
  (resp/json {:hello "world"}))

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
      [{:type "email" :name "E-mail"}
       {:type "text" :name "Name" :placeholder "Optional"}
       {:type "password" :name "Password"}]
      [{:eclass :.btn-primary
        :value "Sign Up"}])))

(defpage signup-check [:post "/signup"] []
  (resp/json {:herp "derp"}))

(defpage about "/about" []
  (common/layout
    [:div.page-header [:h1 "About"]]
    [:p "derpderpderp"]))
