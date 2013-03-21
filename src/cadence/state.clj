(ns cadence.state
  "Provide easy access to application level options."
  (:require [cadence.config :refer [read-config]])
  (:refer-clojure :exclude [get merge]))

(def state
  "A map describing the state of the application."
  (atom {:mode :dev :port 80}))

(def get
  "Get a value for the given key from the current state."
  (partial clojure.core/get @state))

(defn merge
  "Get a value for the given key from the current state."
  [new-state]
  (swap! state (fn [old-state] (clojure.core/merge old-state new-state))))

(defn compute
  "Compute the state from the given environment."
  ([options]
   (let [mode (:mode options
                     (if (= (read-config "PRODUCTION" "no") "yes")
                       :production
                       :development))
         port (:port options
                     (Integer. (read-config "PORT" "5000")))]
     (merge {:mode mode :port port})))
  ([] (compute {})))

(defn development?
  "Return if the application is in development mode."
  []
  (= (get :mode) :development))

(defn production?
  "Return if the application is in development mode."
  []
  (= (get :mode) :production))
