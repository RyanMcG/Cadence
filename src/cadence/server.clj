(ns cadence.server
  (:require [noir.server :as server]))

(server/load-views "src/cadence/views/")

(defn https-url [request-url]
  (str "https://"
       (:server-name request-url)
       (let [port (:server-port request-url)]
         (if (nil?  port) (str ":" port)))
       (:uri request-url)))

(defn require-https [handler]
  (fn [request]
    (if (= (:scheme request) :http)
      (ring.util.response/redirect (https-url request))
      (handler request))))


(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "5000"))
        url "cadence.herokuapp.com"]
    (if (not= mode :dev) (server/add-middleware require-https))
    (server/start port {:mode mode
                        ;:base-url (if (= mode :dev) (str "test" url))
                        :ns 'cadence})))
