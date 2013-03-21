(ns cadence.migrations
  "Define some migrations yo."
  (:require [cadence.model.migration :refer [defmigration]]
            (monger [collection :as mc]
                    [operators :refer :all]))
  (:import (org.bson.types ObjectId)))

(defn str-id
  "Helper function for generating an id when adding migrations."
  [] (str (ObjectId.)))

;; ## Define some migrations.

(defmigration "Add roles to users"
  "5108749844ae8febda9c2ed4"
  (mc/update "users" {} {$set {:roles [:user]}} :multi true)
  (mc/update "users" {} {$unset {:roles ""}} :multi true))
