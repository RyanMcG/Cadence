(ns cadence.views.landing
  (:require [cadence.views.common :as common]
            [noir.response :as response]
            [noir.cookies :as cook])
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
           [:p "WELCOME TO THE LOGIN PAGE!"]
           (:form [:post "/login"]
                    [:input "Username"]
                    [:input "Password"]
                    [:input "Login"])))

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
