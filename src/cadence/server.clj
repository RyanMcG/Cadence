(ns cadence.server
  (:require [noir.server :as server]
            [noir.cljs.core]))

(server/load-views "src/cadence/views/")

(def cljs-options {:advanced {}})

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (noir.cljs.core/start mode cljs-options)
    (server/start port {:mode mode
                        :ns 'cadence})))

