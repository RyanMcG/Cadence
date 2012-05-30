(ns cadence.pattern-recognition
  (:require [noir.session :as sess]
            [clj-ml.data :as data])
  (:use [clj-ml.classifiers]))

(def training-min (atom 6))

(defn pick-cadences
  "Modifies the given session map by modifying the training-cadences key by
  conjoing its value with ncads."
  [session ncad]
  ; TODO Actually make a desciion here instead of always conjoining.
  (let [trcads-key "training-cadences"
        cads (get session trcads-key)]
    (assoc session trcads-key (if (nil? cads)
                                #{ncad}
                                (conj cads ncad)))))

(defn keep-cadence
  "Modifies temporary storage of cadences either adding the new cadence,
  removing an old one, or neither."
  [cadence]
  (let [trcads (sess/get :training-cadences false)]
    ; When the training-cadences key does not exist in the session initialize
    ; it.
    (when-not trcads (sess/put! :training-cadences #{}))
    (sess/swap! pick-cadences cadence)))

(defn kept-cadences
  "A helper function to get the set of training from the session."
  [] (sess/get :training-cadences #{}))

(defn gen-phrase-classifier
  "Creates a trained SVM classifier using the given dataset."
  [training-data]
  (classifier-train
    (make-classifier :support-vector-machine :smo)
    training-data))

(defn cadence-to-vector [class cadence]
  (vec (cons (name class) (map #(get % :timeDifference) (:timeline cadence)))))

(defn create-dataset
  "Creates a dataset from the two sequences of bad and cadences."
  [[bad-cadences good-cadences]]
  ; TODO Implement
  (let [cadvecs (concat
                  (map (partial cadence-to-vector :bad) bad-cadences)
                  (map (partial cadence-to-vector :good) good-cadences))
        attrs (cons {:kind [:good :bad]}
                    (for [x (range (count (:timeline (first good-cadences))))]
                      (keyword (str "c" x))))
        dataset (data/make-dataset :Cadences
                                   attrs
                                   cadvecs
                                   {:class :kind})]
    dataset))

(defn cadence-to-instance
  "Converts a cadence to an instance to be used with a classifier."
  [dataset class cadence]
  (data/make-instance dataset (cadence-to-vector class cadence)))

(defn evaluate-classifier
  "Does a simple cross-valiadation on the given classifier map."
  [{:keys [classifier dataset]}]
  (classifier-evaluate classifier :cross-validation dataset 10))

(defn classify-cadence
  "Returns whether the given cadence is authentic or not."
  [classifier cadence]
  (classifier-classify
    (:classifier classifier)
    (cadence-to-instance (:dataset classifier) :good cadence)))
