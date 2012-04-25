(ns cadence.views.landing
  (:require [cadence.views.common :as common])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpage root "/" []
         (common/layout
           [:h1 "Cadence"]
           [:p "Welcome to cadence"]))

(defpage signup "/signup" []
         (common/layout
           [:h1 "Login or Sign Up"]
           [:p "WELCOME TO THE LOGIN PAGE!"]))

(defpage about "/about" []
         (common/layout
           [:h1 "About"]
           [:p "derpderpderp"]))
