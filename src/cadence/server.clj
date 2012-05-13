(ns cadence.server
  (:require [noir.server :as server]
            [noir.response :as response]
            [noir.cljs.core :as cljs]
            [cemerick.friend :as friend]
            [cadence.model :as model])
  (:use [ring.middleware.gzip :only [wrap-gzip]]
        [cadence.security :only [friend-settings]]))

(server/load-views-ns 'cadence.views)

(defn https-url
  "Creates string from request with scheme as https."
  [request-url]
  (str "https://"
       (:server-name request-url)
       (let [port (:server-port request-url)]
         (if (nil?  port) (str ":" port)))
       (:uri request-url)))

(defn require-https
  "Function generates ring handler to redirect to https."
  [handler]
  (fn [request]
    (if (= (:scheme request) :http)
      (response/redirect (https-url request))
      (handler request))))

(def cljs-options {})

(defn -main "Main function to launch the Cadence application" [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "5000"))
        url "cadence.herokuapp.com"]
    (cljs/start mode cljs-options)
    (if (not= mode :dev) (server/add-middleware require-https))
    (server/add-middleware wrap-gzip)
    (server/add-middleware friend/authenticate friend-settings)
    (model/connect)
    (server/start port (let [opts {:mode mode
                                   :ns 'cadence}]
                         (if (not= mode :dev)
                           (assoc opts :base-url url)
                           opts)))))
