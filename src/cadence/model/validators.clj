(ns cadence.model.validators
  (:require [cadence.model.recaptcha :as recaptcha])
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
  (rule (min-length? name 5)
        [:name "Your name must be at least 5 characters long."])
  (rule (max-length? name 64)
        [:name "Your name must be no more than 64 characters long."])
  (rule (re-find #"^[a-zA-z][\-'\w]*( [\-'\w]+)*\w$" name)
        [:name (str "Your name must match this regex "
                    "/^[a-zA-z][\\-'\\w]*( [\\-'\\w]+)*\\w$/")])
  (rule (min-length? password 8)
        [:password "Passwords must be at least 8 characters long."])
  (rule (max-length? password 64)
        [:password "Passwords must be no more than 64 characters long."])
  (rule (= password repeat-password)
        [:repeat-password "Your passwords did not match!"])
  (rule (or (is-email? email) (empty? email))
        [:email "Emails are optional, but the one you entered is invalid."])
  (rule (recaptcha/check recaptcha_challenge_field recaptcha_response_field)
        [:humans-only (if (empty? recaptcha_response_field)
                        "You forgot to prove you aren't a bot!"
                        "Sorry, your captcha was wrong.")])
  (not (errors? :username :password :repeat-password
                :name :email
                :human-only)))
