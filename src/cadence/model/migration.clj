(ns cadence.model.migration
  (:require (cadence [config :refer [storage]]
                     [model :as model])
            [ragtime.core :as rag]
            (monger [core :as mo]
                    [collection :as mc]
                    [operators :refer :all])
            [monger.ragtime :as monrag])
  (:import (org.bson.types ObjectId)))


;; If not already connected to a database make a connection
(defn connect-if-necessary []
  "If not already connected, connect to the mongodb."
  (when-not (bound? #'mo/*mongodb-database*)
    (model/connect storage)))

(def migrations (atom []))

(defn migration [id up down]
  {:id (ObjectId. id)
   :up (fn [db] (mo/with-db db (eval up)))
   :down (fn [db] (mo/with-db db (eval down)))})

(defmacro add-migrations
  "Take in an arbiraty number of migrations where each is a syntax quoted form
  with three root elements. Apply the migration function to each of these
  forms. Each form should look something like:

       `(\"somemongoobjectidhash\"
           (up form)
           (down form))
  "
  [& more-migrations]
  `(swap! migrations
          concat
          (map (partial apply migration)
               [~@more-migrations])))

(defn str-id
  "Helper function for generating an id when adding migrations."
  [] (str (ObjectId.)))

;; Actually defined some migrations.
(add-migrations
  `("5108749844ae8febda9c2ed4"
     (mc/update "users" {} {$set {:roles [:user]}} :multi true)
     (mc/update "users" {} {$unset {:roles ""}} :multi true)))

(defn run-migrations
  "Run defined migrations."
  ([db]
   (connect-if-necessary)
   (rag/migrate-all db @migrations))
  ([] (run-migrations mo/*mongodb-database*)))
