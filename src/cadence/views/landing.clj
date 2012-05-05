(ns cadence.views.landing
  (:require [cadence.views.common :as common]
            [noir.response :as response])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpage root "/" []
  (common/layout
    [:h1 "Cadence"]
    [:p "Welcome to cadence"]))

(defpage login "/login" []
  (common/layout
    [:h1 "Login"]
    [:form#login.well.form-inline {:action "/login" :method "POST"}
     [:input.input-small
      {:type "text" :name "username" :placeholder "Username"}] " "
     [:input.input-small
      {:type "password" :name "password" :placeholder "Password"}] " "
     [:button.btn {:type "submit"} "Log In"]]))

(defpage login-check [:post "/login"] []
  (response/json {:hello "world"}))

(defpage signup "/signup" []
  (common/layout
    [:h1 "Sign Up"]
    [:form#login.well.form-horizontal {:action "/signup" :method "POST"}
     [:fieldset
      [:div.control-group
      [:label.control-label {:for "username"} "Username: "]
      [:div.controls [:input {:type "text" :name "username" :placeholder ""}]]]
      [:div.control-group
      [:label.control-label {:for "email"} "Email: "]
      [:div.controls [:input {:type "email" :name "email" :placeholder "you@youremail.com"}]]]
      [:div.control-group
      [:label.control-label {:for "password"} "Password: "]
      [:div.controls [:input {:type "password" :name "password"}]]]
      [:div.form-actions [:button.btn.btn-primary {:type "submit"} "Sign In"]]]]))

(defpage signup-check [:post "/signup"] []
  (response/json {:herp "derp"}))

(defpage about "/about" []
  (common/layout
    [:h1 "About"]
    [:p "derpderpderp"]))
