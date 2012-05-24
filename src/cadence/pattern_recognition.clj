(ns cadence.pattern-recognition
  (:require [noir.session :as sess])
  (:use clj-ml.classifiers))

(def training-min (atom 6))

(defn pick-cadences
  "Returns a modfied version of cads which may or may not include ncad depending on its smilarity to them."
  [session ncad]
  ; TODO Actually make a desciion here instead of always conjoining.
  (let [trcads-key "training-cadences"
        cads (get session trcads-key)]
    (assoc session trcads-key (if (nil? cads)
                                        #{ncad}
                                        (conj cads ncad)))))

(defn keep-cadence
  "Modifies temporary storage of cadences either adding the new cadence, removing an old one, or neither."
  [cadence]
  (let [trcads (sess/get :training-cadences false)]
    (when-not trcads (sess/put! :training-cadences #{}))
    (sess/swap! pick-cadences cadence)))

(defn kept-cadences
  "A helper function to get the set of training from the session."
  [] (sess/get :training-cadences #{}))

(defn gen-phrase-classifier
  "Creates an SVM classifier from the given data."
  []
  ; TODO Implement
  nil)

(defn is-authentic?
  "Returns whether the given cadence is authentic or not."
  [classifer cadence]
  ; TODO Implement
  true)