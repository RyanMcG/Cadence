(defproject cadence "0.2.0-SNAPSHOT"
  :description "Use pattern recognition to match users with Cadence.js
               output."
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [com.novemberain/monger "1.0.0-beta4"]
                 [amalloy/ring-gzip-middleware "0.1.1"]
                 [com.cemerick/friend "0.0.7"]
                 [noir "1.2.2"]]
  :main cadence.server)
