(ns cadence.security
  (:require [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [cadence.model.flash :as flash]
            [noir.response :as resp]
            [cadence.model :as model])
  (:use [noir.core :only [pre-route]]))

(def friend-settings
  {:credential-fn (partial creds/bcrypt-credential-fn model/get-user)
   :workflows [(workflows/interactive-form)]
   :login-uri "/login"
   :unauthorized-redirect-uri "/login"
   :default-landing-uri "/"})

(pre-route [:any "/user/*"] {:as req}
           (friend/authenticated nil))

(pre-route [:any "/login"] {:as req}
           (when-not (friend/anonymous?)
             (flash/put! :warning
                         "You must " [:a {:href "/logout"} "logout"]
                         " before you can log in again.")
             (resp/redirect "/user/profile")))

(pre-route [:any "/signup"] {:as req}
           (when-not (friend/anonymous?)
             (flash/put! :warning
                         "You need to " [:a {:href "/logout"} "logout"]
                         " before creating an account.")
             (resp/redirect "/user/profile")))
