(ns cadence.model
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [cadence.model.validators :as is-valid])
  (:use [cadence.config :only [config]]))

(defn- dbconnect [connection-info]
  (if (:uri connection-info)
    (mg/connect-via-uri! (:uri connection-info))
    (mg/connect!)))

(let [db-name "cadence-test"
      coninfo (:storage config)]
  (dbconnect coninfo)
  (mg/authenticate db-name (:username coninfo) (into-array Character/TYPE (:password coninfo)))
  (mg/set-db! (mg/get-db db-name)))

(defn add-user [user]
  (if (is-valid/user? user)
    (mc/insert "users" user)))
