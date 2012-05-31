(ns cadence.config
  (:require [clojure.string :as string]
            [clojure.java.io :as io])
  (:import [java.net URL]))

;; Define a map read from config.clj on the resource path.
(def config-from-file
  (if-let [cres (io/resource "config.clj")]
    (-> cres (.getPath) (load-file)) {}))

(defn read-config
  "Reads config by first accessing the map from the config file and falling back
  on environment variables. This is heroku \"friendly\"."
  [config-var]
  (get config-from-file
       (keyword (string/replace
                  (string/lower-case config-var)
                  "_" "-")) (System/getenv config-var)))

;; Below we define some standard configuration values using read-config.

;; These values are meant for setting up mongo.
(def storage {:uri (read-config "MONGOHQ_URL")
              :db-name (read-config "MONGO_DB_NAME")
              :username (read-config "MONGO_USERNAME")
              :password (read-config "MONGO_PASSWORD")})

;; Various tokens used by the application. At the moment Twitter is not being
;; used, but hopefully it will be in the future.
(def tokens
  {:recaptcha
   {:public-key (read-config "RECAPTCHA_PUBLIC_KEY")
    :private-key (read-config "RECAPTCHA_PRIVATE_KEY")}
   :twitter
   {:token (read-config "TWITTER_OAUTH_TOKEN")
    :secret-token (read-config "TWITTER_OAUTH_SECRET_TOKEN")}})
