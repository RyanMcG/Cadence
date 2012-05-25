(defproject cadence "0.2.0-SNAPSHOT"
  :description "Use pattern recognition to match users with Cadence.js
               output."
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-marginalia "0.7.0"]]
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [noir "1.2.2"]
                  ;:exclusions [org.codehaus.jackson/jackson-core-asl]
                 [net.tanesha.recaptcha4j/recaptcha4j "0.0.7"]
                 [com.novemberain/monger "1.0.0-beta4"]
                 ;[clauth "1.0.0-rc1"]
                 [amalloy/ring-gzip-middleware "0.1.1"]
                 [ring-middleware-format "0.1.1"]
                 ;[fuziontech/ring-json-params "0.2.0"]
                 [com.cemerick/friend "0.0.8"]
                 ;[com.ml/libsvm "2.89"]
                 ;[svm-clj "0.0.1-SNAPSHOT"]
                 ;[edu.berkely.compbio/jlibsvm "0.902"]
                 ;[clj-ml "0.0.3-SNAPSHOT"]
                 [com.leadtune/clj-ml "0.2.1"]]
  :main cadence.server)
