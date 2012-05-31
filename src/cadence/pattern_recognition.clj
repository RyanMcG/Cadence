(ns cadence.pattern-recognition
  (:require [noir.session :as sess]
            [clj-ml.data :as data])
  (:use [clj-ml.classifiers]))

;; Definine a minimum number of cadences necessary for training.
(def training-min (atom 6))

;; This is a helper function for keep cadences. It does the actually
;; modification of the session map deciding whether to include the new cadence
;; with the rest, or not.
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

;; Just a nice accessor function.
(defn kept-cadences
  "A helper function to get the set of training from the session."
  [] (sess/get :training-cadences #{}))

(defn gen-phrase-classifier
  "Creates a trained SVM classifier using the given dataset with a RBF kernel."
  [training-data]
  (classifier-train
    (make-classifier
      :support-vector-machine :smo
      ;:decision-tree :c45
      ;:bayes :naive
      ;:decision-tree :boosted-stump
      ;:neural-network :multilayer-perceptron
      {:fit-logistic-models true
       :reduce-error-pruning true
       :kernel-function {:radial-basis {:gamma 0.02}}
       :random-seed (long (* (rand) Integer/MAX_VALUE))
       :normalize 1 ; When set to 1 it means standardize
       ;:folds-for-cross-validation 30
       })
    training-data))

;; ### Dataset Creation
;;
;; The first thing we need to create a dataset is a vector for each data point.
(defn cadence-to-vector
  "Converts a cadence to a vector prepending whether it is \"good or bad.\""
  [class cadence]
  (vec (cons (name class) (map #(get % :timeDifference) (:timeline cadence)))))

;; The dataset is created by converting the given bad and good cadences into
;; vectors and feeding them into clj-ml's ``make-dataset`` function.
(defn create-dataset
  "Creates a dataset from the two sequences of bad and cadences."
  [[bad-cadences good-cadences]]
  (println "Creating Dataset\n\t Bad Count:" (count bad-cadences)
           "\n\tGood Count:" (count good-cadences))
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

;; ### Classification and Evaluation
;;
;; To do classification we need to be able to tunr a cadence into and instance.
(defn cadence-to-instance
  "Converts a cadence to an instance to be used with a classifier."
  [dataset class cadence]
  (data/make-instance dataset (cadence-to-vector class cadence)))

;; In order to give the user some decent feed back about what's going on we use
;; this function and print out the results later.
(defn evaluate-classifier
  "Does a simple cross-valiadation on the given classifier map."
  [{:keys [classifier dataset]}]
  (classifier-evaluate classifier :cross-validation dataset 30))

(defn classify-cadence
  "Returns whether the given cadence seems authentic ``:good`` or not ``:bad``."
  [classifier cadence]
  (let [inst (cadence-to-instance (:dataset classifier) :good cadence)
        classification (classifier-classify (:classifier classifier)
                                            inst)]
    (keyword (.value (.classAttribute inst) (int classification)))))

(defn interpret-classifier-options
  "Takes a sequence of classifier options and interprets them."
  [options]
  (replace {"-M" :fit-logistic-models
            "-W" :random-seed
            "-P" :epsilon-for-error-round-off
            "-L" :tolerance
            "-N" :normailze ; 0 = Normailize, 1 = Standardize, 2 = neither
            "-K" :kernel-class}
           options))
