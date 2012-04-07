(ns cadence.views.landing
  (:require [cadence.views.common :as common]
            [noir.content.pages :as pages])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpage "/" []
         (common/layout
           [:h1 "Cadence"]
           [:p "Welcome to cadence"]))
