(ns cadence.state
  "Provide easy access to application level options."
  (:require [cadence.config :refer [read-config]])
  (:refer-clojure :exclude [get merge get-in]))

(def state
  "A map describing the state of the application."
  (atom {}))

(defn get [& args]
  "Get a value for the given key from the current state."
  (apply clojure.core/get @state args))

(defn get-in [& args]
  "Get a value for the given path from the current state."
  (apply clojure.core/get-in @state args))

(defn merge
  "Get a value for the given key from the current state."
  [& new-states]
  (apply swap! state clojure.core/merge new-states))

(defn get-defaults
  "Compute and return default options"
  []
  {:port (Integer. (read-config "PORT" "5000"))
           :mode (if (= (read-config "PRODUCTION" "no") "yes")
                   :production
                   :development)
           :thread-count (Integer. (read-config "THREAD_COUNT" "4"))})

(defn merge-with-defaults
  "Compute default options and merge them and given options into current state."
  ([options]
   (merge (get-defaults) options))
  ([] (merge-with-defaults {})))

(defn development?
  "Return if the application is in development mode."
  []
  (= (get :mode) :development))

(defn production?
  "Return if the application is in development mode."
  []
  (= (get :mode) :production))
