(ns cadence.views.landing
  (:require [cadence.views.common :as common]
            [noir.response :as response]
            [noir.cookies :as cook])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers))

(defpage root "/" []
  (common/layout
    [:h1 "Cadence"]
    [:p "Welcome to cadence"]))

(defn alabel [name] (label name name))

(defpartial form-with-labels
  "Returns labels and text fields for each name in the given collection."
  [coll]
  (for [field coll]
    (alabel field)
    ((if ((.toLower field) == "password") password-field text-field) field)))

(defpage login "/login" []
  (common/layout
    [:h1 "Login"]
    [:p "WELCOME TO THE LOGIN PAGE!"]
    (with-group "login"
                (form-to [:post "/login"]
                         (form-with-labels ["Username" "Password"])
                         (submit-button "Login")))))

(defpage login-check [:post "/login"] []
  (response/empty {:hello "world"}))

(defpage signup "/signup" []
  (common/layout
    [:h1 "Login"]
    [:p "WELCOME TO THE LOGIN PAGE!"]))

(defpage about "/about" []
  (common/layout
    [:h1 "About"]
    [:p "derpderpderp"]))
