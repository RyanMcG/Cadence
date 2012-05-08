(ns cadence.security
  (:require [cemerick.friend :as friend]))

(def wrap-security
  (friend/authorize))
