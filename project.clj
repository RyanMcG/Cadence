(defproject cadence "0.1.0-SNAPSHOT"
            :description "Use pattern recognition to match users with Cadence.js
                         output."
            :dependencies [[org.clojure/clojure "1.3.0"]
                           [noir "1.2.0"]]
            :dev-dependencies [[lein-cljsbuild "0.0.13"]] ; cljsbuild plugin
            :cljsbuild {
                        :source-path "src-cljs"
                        :compiler {
                         :output-to "resources/public/js/cljs.js"
                         :optimizations :simple
                         :pretty-print true } }
            :repl-init cadence.repl
            :main cadence.server)
