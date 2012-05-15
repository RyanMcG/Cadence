(ns cadence.remotes
  (:require [cadence.views.landing :as vl]
            [noir.response :as resp])
  (:use noir.fetch.remotes))

(defremote login [&params]
  (apply vl/login params))

(defremote check-phrase [&params]
  {:cool "bro"})
