(ns cadence.model.validators
  (:require [cadence.model :as m]
            [noir.session :as sess]
            [cadence.model.recaptcha :as recaptcha])
  (:use noir.validation))

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

(defn cadence?
  "Tests whether the given cadence is valid or not."
  [cadence for-auth?]
  (let [{:keys [timeline phrase]} cadence
        cad-keys (into #{} (keys cadence))
        phrase-key (if for-auth?
                     :auth-phrase
                     :training-phrase)]
    (rule (= cad-keys #{:timeline :phrase})
          [:cadence (str "User input has incorrect keys."
                         "Got: '" cad-keys
                         "' should be: '#{:timeline :phrase}'")])
    (rule (and
            ; Veirfy that both parts exist
            (not (nil? phrase))
            (not (nil? timeline))
            ; Make sure the length of timeline is the same is the length of the
            ; phrase
            (= (count timeline) (.length phrase))
            ; Ensure that each event contains the correct keys of the proper types
            (reduce (fn [x y] (and x (timeline-event? y)))
                    true timeline)
            ; Verify that the times and timeDifferences add up properly
            (get (reduce (fn [x y]
                           (let [new-time (+ (:time x) (:timeDifference y))
                                 new-ok (= new-time (:time y))]
                             {:ok new-ok :time new-time}))
                         {:ok true :time 0} timeline) :ok)
            (= phrase (reduce (fn [x y] (str x (:character y))) "" timeline)))
          [:cadence "The returned timeline is invalid."])
    (rule (not (nil? (sess/get phrase-key)))
          [:cadence "There is no phrase in the session to compare to."])
    (rule (= phrase (:phrase (sess/get phrase-key)))
          [:cadence "The input phrase does not match the given one."])
    (not (errors? :cadence))))
