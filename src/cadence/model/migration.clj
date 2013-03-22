(ns cadence.model.migration
  (:require (cadence [config :refer [storage]]
                     [model :as model]
                     [state :as state])
            [ragtime [core :as rag]
                     [strategy :as strat]]
            (monger [core :as mo]
                    [collection :as mc]
                    [operators :refer :all])
            (clojure [set :refer [union]])
            [monger.ragtime :as monrag])
  (:import (org.bson.types ObjectId)))

;; If not already connected to a database make a connection
(defn connect-if-necessary []
  "If not already connected, connect to the mongodb."
  (when-not (bound? #'mo/*mongodb-database*)
    (model/connect storage)))

(defmacro defmigration [doc-string id up down]
  "Define migrations as easily as possible."
  `(rag/remember-migration
     ^{:doc ~doc-string} {:id (ObjectId. ~id)
                          :up (fn [db#] (mo/with-db db# ~up))
                          :down (fn [db#] (mo/with-db db# ~down))}))

(defn list-migrations
  ([db]
  (let [defined (vals @rag/defined-migrations)
        applied-ids (rag/applied-migration-ids db)]
    (map (fn [migration]
           (let [doc (:doc (meta migration))
                 id (:id migration)]
             {:id (str id)
              :doc doc
              :applied? (contains? applied-ids (:id migration))}))
         defined)))
  ([] (list-migrations mo/*mongodb-database*)))

(defn run-migrations
  "Run defined migrations."
  ([db strategy]
   (connect-if-necessary)
   (rag/migrate-all db @rag/defined-migrations strategy))
  ([strategy] (run-migrations mo/*mongodb-database* strategy))
  ([] (run-migrations mo/*mongodb-database* (if (state/development?)
                                              strat/apply-new
                                              strat/raise-error))))
