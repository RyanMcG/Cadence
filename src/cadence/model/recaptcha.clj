(ns cadence.model.recaptcha
  (:require [cadence.config :as config])
  (:import (net.tanesha.recaptcha ReCaptcha ReCaptchaFactory)))


(def ^:dynamic *recaptcha*
  (let [rconf (:recaptcha config/tokens)]
    (ReCaptchaFactory/newReCaptcha
      (:public-key rconf) (:private-key rconf) false)))

(defn get-html
  "Returns the html to show the recaptcha."
  (^String [^ReCaptcha recap errors] (.createRecaptchaHtml recap errors nil))
  (^String [errors] (get-html *recaptcha* errors)))

(defn check
  "Checks whether the recaptcha response is correct or not."
  ([^String address
    ^String challenge
    ^String response] (check *recaptcha* address challenge response))
  ([^ReCaptcha recap
    ^String address
    ^String challenge
    ^String response] (.checkAnswer recap address challenge response)))

(defmacro with-recaptcha
  "Macro to rebind the default recaptcha with the given one."
  [recap & body]
  `(binding [*recaptcha* ~recap]
     ~@body))
