(ns cadence.state
  "Provide easy access to application level options."
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

(defn development?
  "Return if the application is in development mode."
  []
  (= (get :mode) :development))

(defn production?
  "Return if the application is in development mode."
  []
  (= (get :mode) :production))
