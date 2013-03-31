(defproject cadence "0.3.2-SNAPSHOT"
  :description "Use pattern recognition to match users with Cadence.js output."
  :url "https://cadence.herokuapp.com/"
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:marginalia {:css ["/docs/marginalia.css"]}}
             :production {:offline true
                          :mirrors {#"central|clojars"
                                    "http://s3pository.herokuapp.com/clojure"}}}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [com.cemerick/friend "0.1.5"]
                 [lib-noir "0.4.6"]
                 [ragtime/ragtime.core "0.3.2"]
                 [org.clojars.ryanmcg/ring-anti-forgery "0.3.1-SNAPSHOT"]
                 [compojure "1.1.3"]
                 [hiccup "1.0.2"]
                 [http-kit "2.0.0"]
                 [bultitude "0.1.7"]
                 [com.cemerick/drawbridge "0.0.6"]
                 [net.tanesha.recaptcha4j/recaptcha4j "0.0.8"]
                 [com.novemberain/monger "1.4.2"]
                 [amalloy/ring-gzip-middleware "0.1.1"]
                 [ring-middleware-format "0.1.1"]
                 [com.leadtune/clj-ml "0.2.4"]]
  :main cadence.server)
