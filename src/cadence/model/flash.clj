(ns cadence.model.flash
  (:refer-clojure :exclude [get])
  (:require [noir.session :as sess]))

(defn put! [t m]
  (sess/flash-put! {:type t :message m}))

(def get sess/flash-get)
