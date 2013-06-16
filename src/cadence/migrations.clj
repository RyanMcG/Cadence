(ns cadence.migrations
  "Define some migrations yo."
  (:require [cadence.model.migration :refer [defmigration]]
            (monger [collection :as mc]
                    [conversion :refer [to-object-id]]
                    [operators :refer :all]))
  (:import (org.bson.types ObjectId)))

(defn- str-id
  "Helper function for generating an id when adding migrations."
  [] (str (ObjectId.)))

;; ## Define some migrations.

(defmigration "Add roles to users"
  "5108749844ae8febda9c2ed4"
  (do (mc/update "users" {} {$set {:roles [:user]}} :multi true)
      (mc/update "users" {:username "RyanMcG"} {$push {:roles :admin}}))
  (mc/update "users" {} {$unset {:roles ""}} :multi true))

(defmigration "Change my name (just for demo)"
  "51571b7244aedb2065e2a3d7"
  (mc/update "users" {:username "RyanMcG"} {$set {:name "Ryan Vincent McGowan"}})
  (mc/update "users" {:username "RyanMcG"} {$set {:name "Ryan McGowan"}}))

(defmigration "Make ObjectId's in cadences actually objectIds"
  "518081b644aedb9b3d161544"
  (doseq [{:keys [phrase_id user_id] id :_id} (mc/find-maps "cadences" {} [:phrase_id :user_id])]
    (mc/update-by-id "cadences" id {$set {:phrase_id (to-object-id (str phrase_id))
                                          :user_id (to-object-id (str user_id))}}))
  (doseq [{:keys [phrase_id user_id] id :_id} (mc/find-maps "cadences" {} [:phrase_id :user_id])]
    (mc/update-by-id "cadences" id {$set {:phrase_id (str phrase_id)
                                          :user_id (str user_id)}})))
