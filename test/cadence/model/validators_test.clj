(ns cadence.model.validators-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [noir.validation :refer [errors? get-errors *errors*]]
            [cadence.model.validators :refer :all])
  (:import (org.bson.types ObjectId)))

(defn define-errors [f]
  (binding [*errors* (atom {})]
    (f)))

(use-fixtures :each define-errors)

(def example-cadence {:timeline [{:time 0,
                                  :timeDifference 0,
                                  :keyCode 84,
                                  :character "t"}
                                 {:time 203,
                                  :timeDifference
                                  203, :keyCode 79,
                                  :character "o"}
                                 {:time 246,
                                  :timeDifference 43,
                                  :keyCode 32,
                                  :character " "}
                                 {:time 409,
                                  :timeDifference 163,
                                  :keyCode 68,
                                  :character "d"}
                                 {:time 561,
                                  :timeDifference 152,
                                  :keyCode 69,
                                  :character
                                  "o"}]
                      :phrase "to do"})

(def cadence-with-incomplete-phrase (assoc example-cadence :phrase "to d"))
(def cadence-with-missing-timeline (dissoc example-cadence :timeline))

;; Cadence tests
(deftest totally-valid-cadence
  (cadence? example-cadence)
  (is (not (errors? :cadence)) (string/join "\n" (get-errors :cadence))))

(deftest missing-timeline
  (cadence? cadence-with-missing-timeline)
  (is (errors? :cadence))
  (is (re-seq #"^User input has incorrect keys" (first (get-errors :cadence)))))

(deftest invalid-phrase
  (cadence? cadence-with-incomplete-phrase)
  (is (errors? :cadence))
  (is (= (first (get-errors :cadence)) "The timeline is invalid.")))
