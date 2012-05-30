(defproject cadence "0.2.1-SNAPSHOT"
  :description "Use pattern recognition to match users with Cadence.js
               output."
  :url "https://cadence.herokuapp.com/"
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-marginalia "0.7.0"]]
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [noir "1.2.2"]
                 [net.tanesha.recaptcha4j/recaptcha4j "0.0.7"]
                 [com.novemberain/monger "1.0.0-beta4"]
                 [amalloy/ring-gzip-middleware "0.1.1"]
                 [ring-middleware-format "0.1.1"]
                 [com.cemerick/friend "0.0.8"]
                 [com.leadtune/clj-ml "0.2.1"]]
  :main cadence.server)
