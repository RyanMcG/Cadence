(ns cadence.model-test
  (:refer-clojure :exclude [identity])
  (:require [cadence.model :refer :all]
            [cadence.pattern-recognition :as patrec]
            (monger [collection :as mc]
                    [core :as mg]
                    [db :refer [drop-db]])
            [clojure.test :refer :all]))

(def test-db-name "cadence_test")

(defn setup-database [f]
  (mg/with-connection (mg/connect)
    (mg/with-db (mg/get-db test-db-name)
      (f)
      (drop-db))))

(use-fixtures :each setup-database)

(defn populate-phrases []
  (add-phrases "to defeat the huns"
               "completing phrases yay"))

(def ^:private example-users
  "Fixture users"
  [{:username "Bob"
    :name "Bobby Millan"
    :password "ronweasly"}])

(defn populate-users []
  (doseq [user example-users]
    (add-user user)))

(def ^:private time-counter (atom 0))

(defn- next-time []
  (let [diff (- 150 (rand-int 100))
        ret [diff @time-counter]]
    (swap! time-counter + diff)
    ret))

(defn- timeline-event [character]
  (let [[diff total] (next-time)]
    {:timeDifference diff
     :time total
     :character character}))

(defn- generate-timeline
  "Generate a timeline based on the given phrase"
  [{:keys [phrase]}]
  (let [timeline (map timeline-event (seq phrase))]
    (reset! time-counter 0)
    timeline))

(defn- generate-cadence
  [phrase user-id]
  {:phrase_id (:_id phrase)
   :user_id user-id
   :timeline (generate-timeline phrase)
   :phrase (:phrase phrase)})

(defn populate-cadences [user-ids & {:keys [complete] :or {complete true}}]
  (doseq [user-id user-ids
          phrase (mc/find-maps "phrases")
          _ (range (if complete
                     @patrec/training-min
                     (dec @patrec/training-min)))]
      (mc/insert "cadences" (generate-cadence phrase user-id))))

(deftest counting-cadences
  (populate-phrases)
  (populate-users)
  (populate-cadences (map :_id (mc/find-maps "users")))
  (let [phrase-count (mc/count "phrases")
        total-cadences-count (* (count example-users)
                                phrase-count
                                @patrec/training-min)
        user-id (:_id (mc/find-one-as-map "users" {}))
        phrase-id (:_id (mc/find-one-as-map "phrases" {}))]
    (testing "counts are accurate for testing"
      (is (= (count-cadences {}) total-cadences-count))
      (is (= (count-cadences phrase-id user-id) @patrec/training-min)))
    (testing "various arguments can mean the same thing"
      (is (= (count-cadences phrase-id user-id)
             (count-cadences {:phrase_id phrase-id :user_id user-id}))))))

(deftest complete-training-on-phrase
  (populate-phrases)
  (populate-users)
  (let [user-id (:_id (mc/find-one-as-map "users" {}))
        completed-phrase-count (trained-phrases-count-for-user user-id)
        {phrase-id :_id
         user-count :usersCount
         :as phrase} (mc/find-one-as-map "phrases" {})]
    (populate-cadences [user-id] :complete false)
    (testing "cannot call complete-training on a phrase for which the user does
              not have sufficent cadences."
      (is (= (dec @patrec/training-min)
             (count-cadences phrase-id user-id)))
      (is (thrown? AssertionError (complete-training user-id phrase-id)))
      (is (= completed-phrase-count (trained-phrases-count-for-user user-id))))
    (testing "after finishing a phrase then we can complete training"
      (add-cadences phrase-id user-id (generate-cadence phrase user-id))
      (is (complete-training user-id phrase-id))
      (testing "the phrase has been added to the user and counts have been
                incremented"
        (is (= (inc completed-phrase-count)
               (trained-phrases-count-for-user user-id)))
        (is (= (inc user-count) (:usersCount
                                  (mc/find-one-as-map "phrases" {}))))))))
