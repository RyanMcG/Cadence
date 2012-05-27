(ns cadence.model
  (:refer-clojure :exclude [identity])
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [noir.validation :as vali]
            [cadence.pattern-recognition :as patrec]
            [noir.options :as options]
            [cemerick.friend :as friend])
  (:use clojure.walk
        monger.operators
        [cemerick.friend.credentials :only [hash-bcrypt]])
  (:import [org.bson.types ObjectId]))

(defn- ensure-indexes
  "Ensures several indexes to use mongo effectively."
  []
  ; Set up an index on random_point for cadence and phrases so we can
  ; *randomlyish* select a few of them.
  (mc/ensure-index "cadences" {:random_point "2d"})
  (mc/ensure-index "phrases" {:random_point "2d"})
  (mc/ensure-index "phrases" {:users 1})
  (mc/ensure-index "classifiers" {:user_id 1
                                  :phrase_id 1})
  ; The `username` key should be unique.
  (mc/ensure-index "users" {:username 1} {:unique 1 :dropDups 1}))

(defn connect
  "Connect to mongo based on the given connection information."
  [connection-info]
  (if (:uri connection-info)
    (mg/connect-via-uri! (:uri connection-info))
    (mg/connect!))
  (let [db-name (:db-name connection-info)]
    (mg/authenticate db-name
                     (:username connection-info)
                     (into-array Character/TYPE (:password connection-info)))
    (mg/set-db! (mg/get-db db-name))
    ; Set up the indexes necessary for decent performance.
    (ensure-indexes)))

(defn get-user
  "Gets the user with the given username from mongo. Takes optional second
  argument for selecting what fields to include in the result. (Used by `friend`
  for user login)"
  ([username fields] (let [get-f
                           (partial
                             mc/find-one-as-map "users" {:username username})]
                       (if (nil? fields)
                         (get-f)
                         (get-f fields))))
  ([username] (get-user username nil)))

(defn add-user
  "Adds the given, validated user to mongo. Hashes the password with bcrypt."
  [user]
  (mc/insert "users"
             (assoc (select-keys
                      user
                      (for [[k v] user :when (vali/has-value? v)] k))
                    :password (hash-bcrypt (:password user)))))

(defn add-cadences
  "Batch inserts many cadences for the given user."
  [user-id phrase-id cads]
  (mc/insert-batch "cadences"
                   (map (fn [x]
                          (merge x {:user_id user-id
                                    :phrase_id phrase-id
                                    :random_point [(rand) 0]}))
                        cads)))
(defn add-trained-user-to-phrase
  "Adds the given user-id to the array of users for the given phrase."
  [user-id phrase-id]
  (mc/update-by-id "phrases" phrase-id {$addToSet {:users user-id}}))

(defn add-phrases
  "Batch inserts phrases to be used for training and auth."
  [phrases]
  (mc/insert-batch "phrases"
                   (map (fn [x]
                          ; This adds a few fields to each phrase we put in mongo.
                          {:phrase x
                           ; This will be used to store the id's of users who
                           ; have completed training with this phrase.
                           :users []
                           :random_point [(rand) 0]}) phrases)))

(defn identity
  "Returns the username of the currently logged in user."
  [] (get friend/*identity* :current))

(defn get-auth
  "Get the current authenticaion map of the signed in user. This is effectively
  the document from mongodb.

      EXAMPLE:

          {:_id ObjectId(\"4fbe571a593e1633b6dfa6ad\"
           :username \"Johnny\"
           :name \"John Doe\"
           :email \"johnny@example.com\"}"
  [] ((:authentications friend/*identity*) (:current friend/*identity*)))

(defn get-phrase
  "Find a phrase for the given user. If the seconf argument is true find one
  they have already done training for. If it is false find one for which the
  user is untrained."
  [user-id for-auth?]
  (let [result (mc/find-one-as-map "phrases"
                                   (if for-auth?
                                     {:users user-id
                                      :random_point {"$near" [(rand) 0]}}
                                     {:users {$ne user-id}
                                      :random_point {"$near" [(rand) 0]}})
                                   {:phrase 1})]
    ; Changes the random_point for increased randomlyishness.
    (when (not (nil? result))
      (mc/update-by-id "phrases"
                       (:_id result)
                       {$set {:random_point [(rand) 0]}}))
    result))

(defn store-classifier
  "Stores the given classifier with the given user/phrase pair."
  [user-id phrase-id classifier]
  ; TODO Implement
  (mc/insert "classifiers" {:user_id user-id
                            :phrase_id phrase-id
                            :classifier classifier
                            ;; Initialize some counts for statistics
                            :attempts 0
                            :authentications 0
                            :rejections 0
                            :successes 0
                            :failures 0}))

(defn get-classifier
  "Gets the classifier needed for the specified user/phrase."
  [user-id phrase-id]
  (if-let [result (mc/find-one-as-map "classifiers" {:user_id user-id :phrase_id phrase-id})]
    result
    (let [classifier (patrec/gen-phrase-classifier)] ; TODO Correct call to make-classifier
      (store-classifier user-id phrase-id classifier)
      classifier)))
