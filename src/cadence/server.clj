(ns cadence.server
  (:require [noir.server :as server]
            [noir.response :as response]
            [cemerick.friend :as friend]
            [cadence.model :as model]
            [cadence.config :as config])
  (:use [ring.middleware.gzip :only [wrap-gzip]]
        [noir.core :only [pre-route]]
        [ring.middleware.format-params :only [wrap-json-params]]
        ;[ring.middleware.json-params :only [wrap-json-params]]
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

;; Redirect anything after /docs to /docs/index.html.
(pre-route [:any "/doc:any" :any #"^(?!s/index.html).*$"] {:as req}
           (response/redirect "/docs/index.html"))

(defn -main "Main function to launch the Cadence application" [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "5000"))
        url "cadence.herokuapp.com"]
    (if (not= mode :dev) (server/add-middleware require-https))
    (server/add-middleware wrap-gzip)
    (server/add-middleware wrap-json-params)
    (server/add-middleware friend/authenticate friend-settings)
    (try (model/connect config/storage)
      (catch java.io.IOException e
        (println "ERROR: Could not connect to MongoDB."))
      (catch java.lang.NullPointerException e
        (println "ERROR: Could not authenticate with Mongo. See config: \n\t"
                 (str (assoc config/storage :password "**********")))))
    (server/start port (let [opts {:mode mode
                                   :ns 'cadence}]
                         (if (not= mode :dev)
                           (assoc opts :base-url url)
                           opts)))))
