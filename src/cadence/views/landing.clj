(ns cadence.views.landing
  (:require [cadence.views.common :as common])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpage root "/" []
  (common/layout
    [:div.page-header [:h1 "Welcome!"]]
    [:p "Welcome to cadence"]))


(defpage about "/about" []
  (common/layout
    [:div.page-header [:h1 "About"]]
    [:p "derpderpderp"]))
