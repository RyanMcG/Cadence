(ns cadence.model
  (:refer-clojure :exclude [identity])
  (:require (monger [core :as mg]
                    [collection :as mc]
                    [query :as mq]
                    [operators :refer :all])
            [noir.validation :as vali]
            [cadence.pattern-recognition :as patrec]
            (clojure [walk :refer :all]
                     [set :refer [difference]])
            [cemerick.friend :as friend]
            [cemerick.friend.credentials :refer [hash-bcrypt]])
  (:import [org.bson.types ObjectId]))

;; This namespace contains all functions related to manipulating the
;; applications "model" (which is mostly mongo).

(defn str-oid
  "Convert the ObjectId in the _id field to a string."
  [result]
  (when-not (nil? result)
    (assoc result :_id (str (:_id result)))))

(defn ensure-indexes
  "Ensures several indexes to use mongo effectively."
  []
  ; Set up an index on random_point for cadence and phrases so we can
  ; *randomlyish* select a few of them.
  (mc/ensure-index "cadences" {:phrase 1 :user_id 1})
  (mc/ensure-index "cadences" {:phrase 1 :user_id 1 :random_point "2d"})
  (mc/ensure-index "phrases" {:random_point "2d", :users 1})
  (mc/ensure-index "phrases" {:usersCount 1})
  (mc/ensure-index "phrases" {:phrase_id 1})
  (mc/ensure-index "phrases" {:phrase 1} {:unique 1 :dropDups 1})
  (mc/ensure-index "classifiers" {:user_id 1 :phrase 1})
  ; The `username` key should be unique.
  (mc/ensure-index "users" {:username 1} {:unique 1 :dropDups 1}))

(defn connect
  "Connect to mongo based on the given connection information."
  [connection-info]
  (if (:uri connection-info)
    (mg/connect-via-uri! (:uri connection-info))
    (let [db-name (:db-name connection-info)]
      (mg/connect!)
      (mg/authenticate db-name
                       (:username connection-info)
                       (into-array Character/TYPE (:password connection-info)))
      (mg/set-db! (mg/get-db db-name))))
  ; Set up the indexes necessary for decent performance.
  (ensure-indexes))

;; ### Users

(def ^:private security-namespace
  "The namespace where the roles hierarchy is created"
  "cadence.security/")

(defn- namespace-and-keywordize-roles
  "Turn the given vector of strings into a set of keywords."
  [roles]
  (set (map #(keyword (str security-namespace %)) roles)))

(defn identity
  "Returns the username of the currently logged in user."
  [] (get friend/*identity* :current))

(defn current-user
  "Get the current authenticaion map of the signed in user. This is effectively
  the user document from the database.

  EXAMPLE:

  {:_id \"4fbe571a593e1633b6dfa6ad\"
   :username \"Johnny\"
   :name \"John Doe\"
   :email \"johnny@example.com\"}"
  [] ((:authentications friend/*identity*) (:current friend/*identity*)))

(defn get-user
  "Gets the user with the given criteria or username from mongo. Takes optional
  second argument for selecting what fields to include in the result (used by
  `friend` for user login)."
  ([criteria fields]
   (when-let [user-map (mc/find-one-as-map "users"
                                           (if (string? criteria)
                                             {:username criteria}
                                             criteria)
                                           fields)]
     (let [overwrite-fields {:roles (namespace-and-keywordize-roles
                                                    (:roles user-map))
                             ;; The ObjectId type is not serializeable so just
                             ;; make it a string.
                             :_id (str (:_id user-map))}]
       (merge user-map overwrite-fields))))
  ([criteria] (get-user criteria [])))

(defn add-roles-to-users
  "Add the given roles to the user-id(s). If the first argument is a vector then
  all user_ids in that vector will be updated with the given roles. If roles"
  [criteria & roles]
  (mc/update "users"
             criteria
             {$pushAll {:roles (let [roles-1 (first roles)]
                                 (if (vector? roles-1) roles-1 roles))}}
             :multi true))

(defn add-user
  "Adds the given, validated user to mongo. Hashes the password with bcrypt."
  [user]
  (mc/insert "users"
             (merge (select-keys
                      user
                      (for [[k v] user :when (vali/has-value? v)] k))
                    {:password (hash-bcrypt (:password user))
                     :roles [:user]})))

;; ### Cadences

(def get-cadences (partial mc/find-maps "cadences"))

(defn count-cadences
  "Takes a criteria or a phrase-id and user-id followed by optional fields
  vector."
  [criteria-or-phrase-id & args]
  (if (map? criteria-or-phrase-id)
    (apply mc/count "cadences" criteria-or-phrase-id args)
    (let [user-id (first args)
          remaining-args (rest args)]
      (apply mc/count
             "cadences"
             {:phrase_id criteria-or-phrase-id
              :user_id user-id}
             remaining-args))))

(defn- cadence-to-document
  [user-id phrase-id cadence]
  (merge cadence
         {:user_id user-id
          :phrase_id phrase-id
          :random_point [(rand) 0]}))

(defn add-cadences
  "Batch inserts many cadences for the given user."
  [phrase-id user-id & cads]
  (mc/insert-batch "cadences"
                   (map (partial cadence-to-document user-id phrase-id) cads)))

(defn keep-cadence
  "Inserts the fresh-cadence and then removes cadences of the given phrase and
  user id if they are too dissimilar. Returns the number of cadences picked to
  keep."
  [phrase-id user-id fresh-cadence]
  (add-cadences phrase-id user-id fresh-cadence)
  (let [cadences (get-cadences {:phrase_id phrase-id
                                :user_id user-id})
        cadence-ids (set (map :_id cadences))
        picked-cadence-ids (set (map :_id (patrec/pick-cadences cadences)))
        unpicked-cadence-ids (difference cadence-ids picked-cadence-ids)]
    (mc/remove "cadences" {:_ids {$in unpicked-cadence-ids}})
    (count picked-cadence-ids)))

;; ### Phrases

(defn add-trained-user-to-phrase
  "Adds the given user-id to the array of users for the given phrase."
  [user-id phrase-id]
  (mc/update-by-id "phrases" phrase-id {$addToSet {:users user-id}
                                        ;; Increment count by 1 too.
                                        $inc {:usersCount 1}}))

(defn- phrase-to-document
  "Take the given phrase (a String) and wrap it in a map with some other keys
  for storing in the database."
  [^String phrase]
  {:phrase phrase
   ; This will be used to store the id's of users who
   ; have completed training with this phrase.
   :users []
   :usersCount 0
   :random_point [(rand) 0]})

(defn add-phrases
  "Batch inserts phrases to be used for training and auth."
  [& phrases]
  (mc/insert-batch "phrases" (map phrase-to-document phrases)))

(defn- get-random-phrase
  "Randomly as possible get a phrase that matches the given query."
  ([query fields]
    (let [result (mc/find-one-as-map "phrases"
                                     (assoc query
                                            :random_point {"$near" [(rand) 0]})
                                     {:phrase 1}
                                     fields)]
      ; Changes the random_point for increased randomlyishness.
      (when (not (nil? result))
        (mc/update-by-id "phrases"
                         (:_id result)
                         {$set {:random_point [(rand) 0]}}))
      (str-oid result)))
  ([query] (get-random-phrase query [:phrase])))

(defn get-phrase-for-auth
  "Get a random phrase for user authentication."
  [user-id]
  (get-phrase {:users user-id :usersCount {$gt 5}}))

(defn get-phrase-for-training
  "Get a random phrase for user training."
  [user-id]
  (str-oid (get-phrase {:users {$ne user-id}} [:phrase])))

(defn phrase-complete-for-user?
  [phrase-id user-id]
  (>= (count-cadences {:phrase_id phrase-id :user_id user-id})
      @patrec/training-min))

(defn get-training-data
  "Returns cadences to be used to train a classifier as a tuple of bad cadences
  and good cadences."
  [user-id phrase]
  (let [find-cadences (fn [by-user lim]
                        (mq/with-collection "cadences"
                          (mq/find {:phrase phrase
                                    :user_id (if by-user
                                               user-id
                                               {$ne user-id})
                                    :random_point {"$near" [(rand) 0]}})
                          (mq/fields [:timeline :phrase])
                          (mq/limit lim)
                          (mq/snapshot)))]
    [(find-cadences false 200) (find-cadences true 50)]))

(defn untrain-user-phrase
  "User id from users array for the given phrase and the related cadences."
  [user-id phrase-id]
  ;; Remove user from specified phrase
  (let [user-oid (ObjectId. user-id)
        phrase-oid (ObjectId. phrase-id)]
    (mc/update-by-id "phrases" phrase-oid
                     {$pull {:users user-oid}
                      $inc {:usersCount -1}})
    ;; Remove related cadences
    (mc/remove "cadences" {:phrase_id phrase-oid
                           :user_id user-oid})))

;; ### Classifiers

(defn store-classifier
  "Stores the given classifier with the given user/phrase pair."
  [user-id phrase classifier]
  ; TODO Implement
  (mc/insert "classifiers" {:user_id user-id
                            :phrase phrase
                            :classifier classifier
                            :type :svm
                            ;; Initialize some counts for statistics
                            :attempts 0
                            :authentications 0
                            :rejections 0
                            :successes 0
                            :failures 0}))

(defn get-classifier
  "Gets the classifier needed for the specified user/phrase. If the classifier
  is not found in the database then generate one."
  [user-id phrase]
  (if-let [result (mc/find-one-as-map "classifiers"
                                      {:user_id user-id :phrase phrase})]
    result
    (let [dataset (patrec/create-dataset (get-training-data user-id phrase))
          classifier (patrec/gen-phrase-classifier dataset)]
      ;(store-classifier user-id phrase classifier)
      {:classifier classifier :dataset dataset})))
