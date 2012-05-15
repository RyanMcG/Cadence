(ns cadence.security
  (:require [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            ;[oauth.client :as oauth]
            [cadence.model :as model])
  (:use [noir.core :only [pre-route]]))

(def friend-settings
      {:credential-fn (partial creds/bcrypt-credential-fn model/get-user)
       :workflows [(workflows/http-basic)]
       :login-uri "/login"
       :unauthorized-redirect-uri "/login"
       :default-landing-uri "/"})

(pre-route [:any "/user.*"] {:as req}
       (friend/authenticated nil))
