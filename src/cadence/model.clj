(ns cadence.model
  (:refer-clojure :exclude [identity])
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [cadence.model.validators :as is-valid]
            [cemerick.friend :as friend])
  (:use clojure.walk
        [cemerick.friend.credentials :only [hash-bcrypt]]))

(defn- ensure-indexes []
  (mc/ensure-index "users" {:username 1}))

(defn connect [connection-info]
  (if (:uri connection-info)
    (mg/connect-via-uri! (:uri connection-info))
    (mg/connect!))
  (let [db-name (:db-name connection-info)]
    (mg/authenticate db-name
                     (:username connection-info)
                     (into-array Character/TYPE (:password connection-info)))
    (mg/set-db! (mg/get-db db-name))
    (ensure-indexes)))

(defn get-user [username]
  (mc/find-one-as-map "users" {:username username}))

(defn add-user [user]
  (if (is-valid/user? user)
    (mc/save "users" (assoc user :password (hash-bcrypt (:password user))))))

(def identity #(get friend/*identity* :current))

(defn get-phrase []
  "completing this phrase is fun")
