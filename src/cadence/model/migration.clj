(ns cadence.model.migration
  (:require (cadence [config :refer [storage]]
                     [model :as model]
                     [state :as state])
            [ragtime [core :as rag]
                     [strategy :as strat]]
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

(defmacro defmigration [doc-string id up down]
  "Define migrations as easily as possible."
  `(swap! migrations
          conj
          ^{:doc ~doc-string}
          {:id (ObjectId. ~id)
           :up (fn [db#] (mo/with-db db# ~up))
           :down (fn [db#] (mo/with-db db# ~down))}))

(state/compute)
(def strategy
  "Pick a conflict handling strategy based on the state of the currently running
  server."
  (if (state/development?)
    strat/rebase
    strat/raise-error))

(defn run-migrations
  "Run defined migrations."
  ([db]
   (connect-if-necessary)
   (rag/migrate-all db @migrations strategy))
  ([] (run-migrations mo/*mongodb-database*)))
