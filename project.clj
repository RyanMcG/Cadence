(defproject cadence "0.3.0-SNAPSHOT"
  :description "Use pattern recognition to match users with Cadence.js output."
  :url "https://cadence.herokuapp.com/"
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:plugins [[lein-kibit "0.0.7-SNAPSHOT"]
                             [lein-marginalia "0.7.1"]]}}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [lib-noir "0.3.5"]
                 [ragtime/ragtime.core "0.3.2"]
                 [compojure "1.1.3"]
                 [hiccup "1.0.2"]
                 [http-kit "2.0.0-RC4"]
                 [ring-refresh "0.1.1"]
                 [bultitude "0.1.7"]
                 [com.cemerick/drawbridge "0.0.6"]
                 [net.tanesha.recaptcha4j/recaptcha4j "0.0.8"]
                 [com.novemberain/monger "1.4.2"]
                 [amalloy/ring-gzip-middleware "0.1.1"]
                 [ring-middleware-format "0.1.1"]
                 [com.cemerick/friend "0.1.3"]
                 [com.leadtune/clj-ml "0.2.4"]]
  :marginalia {:css ["/docs/marginalia.css"]}
  :main cadence.server)
