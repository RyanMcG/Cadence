(ns cadence.views.user
  (:require [cadence.views.common :as common]
            [cadence.model.flash :as flash]
            [cemerick.friend :as friend]
            [cadence.model :as m]
            [noir.response :as resp])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpage user "/user/:username" {:keys [username]}
  (if (= username (m/identity))
    (common/layout (str "Welcome " username "!"))
    (do
      (flash/put! :error (str "You cannot access " username "'s page."))
      (resp/redirect (str "/user/" (m/identity))))))
