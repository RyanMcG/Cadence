(ns cadence.server
  (:require [noir.server :as server]))

(server/load-views "src/cadence/views/")

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "5010"))
        url "cadence.ryanmcg.com"]
    (server/start port {:mode mode
                        ;:base-url (if (= mode :dev) (str "test" url))
                        :ns 'cadence})))

