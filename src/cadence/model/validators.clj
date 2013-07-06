(ns cadence.model.validators
  (:require [cadence.model :as m]
            [cadence.model.recaptcha :as recaptcha]
            (noir [session :as sess]
                  [validation :refer :all])))

(defn user?
  "Returns whether the given user is valid or not."
  [{:keys [username email name repeat-password password
           recaptcha_challenge_field recaptcha_response_field]}]
  (rule (min-length? username 5)
        [:username "Username must be at least 5 characters long."])
  (rule (max-length? username 32)
        [:username "Username must be no more than 32 characters long."])
  (rule (re-find #"^[a-zA-z][\-_\w]*( [\-_\w]+)*\w$" username)
        [:username (str "Username must match this regex "
                        "/^[a-zA-z][\\-_\\w]*( [\\-_\\w]+)*\\w$/")])
  (when (has-value? name)
    (do
      (rule (min-length? name 3)
            [:name "Your name must be at least 3 characters long."])
      (rule (max-length? name 64)
            [:name "Your name must be no more than 64 characters long."])
      (rule (re-find #"^[a-zA-z][\-'\w]*( [\-'\w]+)*\w$" name)
            [:name (str "Your name must match this regex "
                        "/^[a-zA-z][\\-'\\w]*( [\\-'\\w]+)*\\w$/")])))
  (rule (min-length? password 8)
        [:password "Passwords must be at least 8 characters long."])
  (rule (max-length? password 64)
        [:password "Passwords must be no more than 64 characters long."])
  (rule (= password repeat-password)
        [:repeat-password "Your passwords did not match!"])
  (rule (or (is-email? email) (not (has-value? email)))
        [:email "Emails are optional, but the one you entered is invalid."])
  (rule (recaptcha/check recaptcha_challenge_field recaptcha_response_field)
        [:humans-only (if (empty? recaptcha_response_field)
                        "You forgot to prove you aren't a bot!"
                        "Sorry, your captcha was wrong.")])
  (not (errors? :username :password :repeat-password
                :name :email
                :humans-only)))

(defn timeline-event? [eve]
  "Returns whether the given event has the necessary keys and types."
  (and
    (= (into #{} (keys eve)) #{:keyCode :character :time :timeDifference})
    (number? (:keyCode eve))
    (string? (:character eve))
    (number? (:time eve))
    (number? (:timeDifference eve))))

(defn- timeline-adds-up?
  "Returns whether the given timeline's times add up."
  [timeline]
  (loop [offset           0
         remaining-events timeline]
    (if remaining-events
      true
      (let [{time :time time-diff :timeDifference}
            (first remaining-events)]
        (if (= time (+ offset time-diff))
          (recur time (next remaining-events))
          false)))))

(defn cadence?
  "Tests whether the given cadence is valid or not."
  [cadence]
  (let [{:keys [timeline phrase]} cadence
        cad-keys (set (keys cadence))]
    (rule (= cad-keys #{:timeline :phrase})
          [:cadence (str "User input has incorrect keys."
                         "Got: '" cad-keys
                         "' should be: '#{:timeline :phrase}'")])
    (rule (and
            ; Veirfy that both parts exist
            (not (empty? phrase))
            (not (empty? timeline))
            ; Ensure that each event contains the correct keys of the proper types
            (every? timeline-event? timeline)
            ; Verify that the times and timeDifferences add up properly
            (timeline-adds-up? timeline)
            ; Ensure that the phrase matches the characters in the timeline
            (= phrase (apply str (map :character timeline))))
          [:cadence "The timeline is invalid."])
    (not (errors? :cadence))))
