(ns cadence.views.response-codes
  (:require [cadence.views.common :as common])
  (:use noir.statuses))

; Set the 404 page to something more fun.
(set-page! 404
           (common/base-layout
             [:div.hero-unit
              [:h1 "Hey! Where are you going?"]
              [:p (str "Maybe it was just an accident, but we can't find that page"
                       " you requested.")]
              [:h2.response-code "404"]]))

(set-page! 500
           (common/base-layout
             [:div.hero-unit
              [:h1 "Oops! Something bad happened."]
              [:p "An error has occured and I can't load a page or anything."]
              [:h2.response-code "500"]]))
