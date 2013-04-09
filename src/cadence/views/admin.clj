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
  [{:keys [id doc applied? source]}]
  (let [badge-class (if applied? "label-success" "")
        badge-icon (if applied? "icon-ok-sign" "icon-remove-sign")
        badge-text (if applied? "Applied" "Not Applied")
        button-class (if applied? "btn-inverse" "btn-primary")
        button-text (if applied? "Rollback" "Apply")
        {:keys [up down]}
        (into {} (for [[k source-form] source]
                   [k (common/format-source-code source-form)]))]
    [:div.migration {:id (str "migration-" id)}
     [:div.meta.row-fluid
      [:div.date.span4 (h (str (time-format/unparse
                                 (:rfc822 time-format/formatters)
                                 (time-coerce/from-long
                                   (.getTime (ObjectId. id))))))]
      [:div.id.span4 [:code (h id)]]
      [:div.doc.span4 [:p (h doc)]]
      ]

     [:div.source.row-fluid
      [:div.span5 [:pre [:code (h up)]]]
      [:div.span5 [:pre [:code (h down)]]]
      [:div.span2
       [:div.controls
        [:button {:data-object-id id :class (str "btn " button-class)}
         button-text]]
       [:div.applied
        [:span {:class (str "label " badge-class)}
         [:i {:class (str "icon-white " badge-icon)}] (str " " badge-text)]]]]]))

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
                        "Apply" migration/migrate-by-id) id)]
    (resp/json {:count (.getField write-result "n")})))

