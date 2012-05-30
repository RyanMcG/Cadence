(ns cadence.server
  (:require [noir.server :as server]
            [noir.response :as response]
            [ring.util.response :as ringresp :only [redirect]]
            [cemerick.friend :as friend]
            [cadence.model :as model]
            [cadence.config :as config])
  (:use [ring.middleware.gzip :only [wrap-gzip]]
        [noir.core :only [pre-route]]
        [ring.middleware.format-params :only [wrap-json-params]]
        ;[ring.middleware.json-params :only [wrap-json-params]]
        [cadence.security :only [friend-settings]]))

(server/load-views-ns 'cadence.views)

;; This helper function is used in requires-https-heroku (below). It was
;; stollen from
;; [cemerick.friend](https://github.com/cemerick/friend/blob/3d9b679f1297a112210f271df6d36e167e206122/src/cemerick/friend.clj#L7).
(defn- original-url
  "Takes a request map and converts it to a string url."
  [{:keys [scheme server-name server-port uri query-string]}]
  (str (name scheme) "://" server-name
       (cond
         (and (= :http scheme) (not= server-port 80)) (str \: server-port)
         (and (= :https scheme) (not= server-port 443)) (str \: server-port)
         :else nil)
       uri
       (when (seq query-string)
         (str \? query-string))))

;; The only difference between this and the one in friend is I also check the
;; x-forwarder-proto key.
(defn requires-https-heroku
  "An https redirect middleware for heroku. Modled after
  cemerick.friend/requires-scheme."
  [handler]
  (fn [request]
    (if (or (= (:scheme request) :https) (= (get (:headers request)
                                                 "x-forwarded-proto")
                                            "https"))
      (handler request)
      (ringresp/redirect
        (original-url (assoc request
                             :scheme :https
                             :server-port 443))))))

;; Redirect anything after /docs to /docs/index.html.
(pre-route [:any "/doc:anything" :anything #"^(?!s/index.html).*$"] {:as req}
           (response/redirect "/docs/index.html"))

(defn -main "Main function to launch the Cadence application" [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "5000"))
        url "https://cadence.herokuapp.com"]
    (when (not= mode :dev) (server/add-middleware requires-https-heroku))
    (server/add-middleware wrap-gzip)
    (server/add-middleware wrap-json-params)
    (server/add-middleware friend/authenticate friend-settings)
    (try
      (model/connect config/storage)
      (server/start port (let [opts {:mode mode
                                     :ns 'cadence}]
                           (if (not= mode :dev)
                             (assoc opts :base-url url)
                             opts)))
      (catch java.io.IOException e
        (println "ERROR: Could not connect to MongoDB."))
      (catch java.lang.NullPointerException e
        (println "ERROR: Could not authenticate with Mongo. See config: \n\t"
                 (str (assoc config/storage :password "**********")))))))
