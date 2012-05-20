(ns cadence.model.recaptcha
  (:require [cadence.config :as config])
  (:use [noir.request :only [ring-request]])
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
  ([^ReCaptcha recap
    ^String challenge
    ^String response]
   (let [req (ring-request)]
     (-> (.checkAnswer recap
                       (get req :remote-addr)
                       challenge response)
       (.isValid))))
  ([^String challenge
    ^String response] (check *recaptcha* challenge response)))

(defmacro with-recaptcha
  "Macro to rebind the default recaptcha with the given one."
  [recap & body]
  `(binding [*recaptcha* ~recap]
     ~@body))
