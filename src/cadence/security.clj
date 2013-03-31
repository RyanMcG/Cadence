(ns cadence.security
  (:require [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [cadence.model.flash :as flash]
            [noir.response :as resp]
            [cadence.model :as model]))

;; Describe user, admin hierarchy.
(derive ::admin ::user)

(def friend-settings
  {:credential-fn (partial creds/bcrypt-credential-fn model/get-user)
   :workflows [(workflows/interactive-form)]
   :login-uri "/login"
   :unauthorized-redirect-uri "/login"
   :default-landing-uri "/"})

(defn wrap-anonymous-only
  "If the user is not anonymous redirect them to his/her profile."
  [handler & message]
  (if (friend/anonymous?)
    handler
    (fn [request]
      (flash/put! :warning message)
      (resp/redirect "/user/profile"))))

(defn admin?
  "Return the role (not nil so its true) that isa? admin."
  ([user] (some #(isa? % ::admin) (:roles user)))
  ([] (admin? (model/get-auth))))
