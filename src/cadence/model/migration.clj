(ns cadence.model.migration
  (:require (monger [core :as mo]
                    [collection :as mc])
            [monger.ragtime]
            [ragtime.core :as rag]
            [ragtime.strategy :as strat]
            (cadence [state :as state]
                     [config :refer [storage]]
                     [model :as model]))
  (:import (org.bson.types ObjectId)))

;; If not already connected to a database make a connection
(defn connect-if-necessary []
  "If not already connected, connect to the mongodb."
  (when-not (bound? #'mo/*mongodb-database*)
    (model/connect storage)))

(defmacro defmigration [doc-string id up down]
  "Define migrations as easily as possible."
  `(rag/remember-migration
     ^{:doc ~doc-string
       :source {:up (quote ~up)
                :down (quote ~down)}}
     {:id (ObjectId. ~id)
      :up (fn [db#] (mo/with-db db# ~up))
      :down (fn [db#] (mo/with-db db# ~down))}))

(def find-migration-by-id (partial mc/find-map-by-id "meta.migrations"))

(defn list-migrations
  ([db]
  (let [defined (vals @rag/defined-migrations)
        applied-ids (rag/applied-migration-ids db)]
    (map (fn [migration]
           (let [meta-map (meta migration)
                 id (:id migration)]
             (merge {:id (str id)
                     :created-at (:created_at (find-migration-by-id id))
                     :applied? (contains? applied-ids id)}
                    meta-map)))
         defined)))
  ([] (list-migrations mo/*mongodb-database*)))

(defn part-rag
  "Partialize ragtime methods by always using our mongo database."
  [ragtime-func]
  (partial ragtime-func mo/*mongodb-database*))

(defn get-migration-by-id
  "Get a full migration map from defined-migrations."
  [id]
  (get @rag/defined-migrations (ObjectId. id)))

(defn migrate-by-id
  "Migrate the migration with the given id."
  [id]
  ((part-rag rag/migrate) (get-migration-by-id id)))

(defn rollback-by-id
  "Rollback to the migration with the given id."
  [id]
  ((part-rag rag/rollback) (get-migration-by-id id)))

(defn run-migrations
  "Run defined migrations."
  ([db strategy]
   (connect-if-necessary)
   (rag/migrate-all db @rag/defined-migrations strategy))
  ([strategy] (run-migrations mo/*mongodb-database* strategy))
  ([] (run-migrations mo/*mongodb-database* (if (state/development?)
                                              strat/apply-new
                                              strat/raise-error))))
