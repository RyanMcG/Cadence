(ns cadence.views.response-codes
  (:require [cadence.views.common :as common]))

(def not-found
  "Define 404 error page."
  (common/base-layout
    [:div.hero-unit
     [:h1 "Hey! Where are you going?"]
     [:p (str "Maybe it was just an accident, but we can't find that page"
              " you requested.")]
     [:h2.response-code "404"]]))

(def server-error
  "Define 500 error page."
  (common/base-layout
    [:div.hero-unit
     [:h1 "Oops! Something bad happened."]
     [:p "An error has occured and I can't load a page or anything."]
     [:h2.response-code "500"]]))
