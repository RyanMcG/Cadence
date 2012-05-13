(ns cadence.model
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [cadence.model.validators :as is-valid])
  (:use [cadence.config :only [storage]]))

(defn connect [connection-info]
  (if (:uri connection-info)
    (mg/connect-via-uri! (:uri connection-info))
    (mg/connect!))
  (let [db-name (:db-name storage)]
    (mg/authenticate db-name (:username storage) (into-array Character/TYPE (:password storage)))
    (mg/set-db! (mg/get-db db-name))))

(defn get-user [username]
  (mc/find "users" {:username username}))

(defn add-user [user]
  (if (is-valid/user? user)
    (mc/insert "users" user)))
