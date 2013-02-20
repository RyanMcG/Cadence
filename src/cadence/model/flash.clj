(ns cadence.model.flash
  (:refer-clojure :exclude [get])
  (:require [noir.session :as sess]))

(defn- flash-now!
  "Store a flash value that will persist for this request only."
  [k v]
  (swap! sess/*noir-flash* assoc-in [:incoming k] v))

(defn- base-put!
  "Most basic flash put the can either do an immediate or slow push using a
  custom format on the :alert key."
  [flasher t m] (flasher :alert {:type t :message (apply str m)}))

(defn- put-partial
  "A wrapper around base-put! that allows it to be used with a variable number
  of arguments. The second argument is treated specially if it is a keyword."
  [flasher]
  (fn [flash-type & message]
    (if (keyword? flash-type)
      (base-put! flasher flash-type message)
      (base-put! flasher :info (cons flash-type message)))))

(def now!
  "Does an immediate flash."
  (put-partial flash-now!))

(def put!
  "Does a slow flash."
  (put-partial sess/flash-put!))

;; Alias this namespace's get to noir's flash-get.
(def get (partial sess/flash-get :alert))
