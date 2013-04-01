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
  [{:keys [id doc applied?]}]
  [:tr.migration {:id (str "migration-" id)}
   [:td.date (h (str (time-format/unparse
                         (:rfc822 time-format/formatters)
                         (time-coerce/from-long
                           (.getTime (ObjectId. id))))))]
   [:td.id [:code (h id)]]
   [:td.doc [:p (h doc)]]
   (let [badge-class (if applied? "label-success" "")
         badge-icon (if applied? "icon-ok-sign" "icon-remove-sign")
         badge-text (if applied? "Applied" "Not Applied")
         button-class (if applied? "btn-inverse" "btn-primary")
         button-text (if applied? "Rollback" "Apply")]
     (html
       [:td.applied [:span {:class (str "label " badge-class)}
                     [:i {:class (str "icon-white " badge-icon)}]
                     (str " " badge-text)]]

       [:td.controls [:button {:data-object-id id
                               :class (str "btn " button-class)}
                      button-text]]))])

(defn migrations
  "A nice place to view migrations."
  [request]
  (common/layout
    [:h2 "Migrations"]
    [:table#migrations.table.table-striped.table-bordered
     [:thead
      [:tr
       [:th.date "Timestamp"]
       [:th.id "ObjectId"]
       [:th.doc "Description"]
       [:th.applied "Applied?"]
       [:th.controls]]]
     [:tbody
      (require :reload 'cadence.migrations)
      (map migration-row (migration/list-migrations))]]))

(defn post-migrations
  "A nice place to view migrations."
  [{{id "object_id" action "action"} :form-params}]
  (let [write-result ((case action
                        "Rollback" migration/rollback-by-id
                        "Apply" migration/migrate-by-id) id)]
    (resp/json {:count (.getField write-result "n")})))

