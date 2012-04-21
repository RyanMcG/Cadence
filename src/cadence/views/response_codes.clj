(ns cadence.views.response_codes
  (:require [cadence.views.common :as common])
  (:use noir.statuses))

(set-page! 404
           (common/base-layout
             [:div.hero-unit
             [:h1 "Hey! Where are you going?"]
             [:p (str "Maybe it was just an accident, but we can't find that page"
                      " you requested.")]
              [:h2.response-code "404"]]))
