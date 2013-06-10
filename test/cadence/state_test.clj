(ns cadence.state-test
  "Provide easy access to application level options."
  (:refer-clojure :exclude [get merge get-in])
  (:require [clojure.test :refer :all]
            [cadence.config :refer [read-config]]
            [cadence.state :refer :all]))

(defn compute-state-and-reset
  [f]
  (merge-with-defaults)
  (f)
  (reset! state {}))

(use-fixtures :each compute-state-and-reset)

(deftest state-changes
  (testing "default state"
    (is (= @state (get-defaults))))
  (testing "overriding default"
    (merge {:thread-count 99})
    (is (= @state (assoc (get-defaults) :thread-count 99)))))
