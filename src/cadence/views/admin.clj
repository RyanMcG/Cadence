(ns cadence.views.admin
  (:require [cadence.views.common :as common]
            [cadence.model.migration :as migration]
            (ragtime [core :as rag])
            (clj-time [coerce :as time-coerce]
                      [format :as time-format])
            [noir.response :as resp])
  (:use (hiccup core page def element util))
  (:import (org.bson.types ObjectId)))

(defhtml migration-row
  "Convert a modified migration map to a table row."
  [{:keys [id doc applied? source created-at]}]
  (let [[badge-class badge-icon badge-text button-class button-text]
        (if applied?
          ["label-success" "icon-ok-sign" "Applied" "btn-inverse" "Rollback"]
          ["" "icon-remove-sign" "Not Applied" "btn-primary" "Apply"])
        hdoc (h doc)
        {:keys [up down]}
        (into {} (for [[k source-form] source]
                   [k (common/format-source-code source-form)]))]
    [:div.migration.row-fluid {:id (str "migration-" id)}
     [:div.left-side.span4
      [:h3.migration-title hdoc]
      (common/meta-table {:date (h (common/human-readable-objectid-datetime id))
                          "object id" [:code (h id)]
                          :doc [:p hdoc]
                          "applied at" (h (if created-at (str created-at)
                                            "N/A"))
                          :applied [:span {:class (str "label " badge-class)}
                                    [:i {:class (str "icon-white " badge-icon)}]
                                    (str " " badge-text)]})
      [:div.controls
       [:button {:data-object-id id :class (str "btn " button-class)}
        button-text]]]
     [:div.right-side.source.span8
      [:div.well
      [:div.row-fluid
       [:h3 "Up"]
       [:pre [:code (h up)]]]
      [:div.row-fluid
       [:h3 "Down"]
       [:pre [:code (h down)]]]]]]))

(defn migrations
  "A nice place to view migrations."
  [request]
  (common/layout
    [:h2 "Migrations"]
    [:div#migrations
     (require :reload 'cadence.migrations)
     (map migration-row (migration/list-migrations))]))

(defn post-migrations
  "A nice place to view migrations."
  [{{id "object_id" action "action"} :form-params}]
  (let [write-result ((case action
                        "Rollback" migration/rollback-by-id
                        "Apply" migration/migrate-by-id) id)
        created-at (:created_at (migration/find-migration-by-id
                                  (ObjectId. id)))]
    (resp/json {:count (.getField write-result "n")
                :createdAt (if created-at (str created-at) "N/A")})))
