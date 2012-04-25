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
