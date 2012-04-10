(defproject cadence "0.1.0-SNAPSHOT"
            :description "Use pattern recognition to match users with Cadence.js
                         output."
            :dependencies [[org.clojure/clojure "1.3.0"]
                           [noir-cljs "0.3.0"]
                           [noir "1.2.0"]]
            :main ^{:skip-aot true} cadence.server)
