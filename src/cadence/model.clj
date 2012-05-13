(ns cadence.model
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [cadence.model.validators :as is-valid]))

(defn connect [connection-info]
  (if (:uri connection-info)
    (mg/connect-via-uri! (:uri connection-info))
    (mg/connect!))
  (let [db-name (:db-name connection-info)]
    (mg/authenticate db-name (:username connection-info) (into-array Character/TYPE (:password connection-info)))
    (mg/set-db! (mg/get-db db-name))))

(defn get-user [username]
  (mc/find "users" {:username username}))

(defn add-user [user]
  (if (is-valid/user? user)
    (mc/insert "users" user)))

(defn get-phrase [] "passwords are so completely last decade")
