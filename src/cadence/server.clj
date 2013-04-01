(ns cadence.server
  (:require [noir.response :as response]
            [ring.util.response :as ringresp :only [redirect]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [cemerick.friend :as friend]
            (cadence [model :as model]
                     [state :as state]
                     [config :refer [storage read-config]]
                     [routes :refer [app-routes]]
                     [security :refer [friend-settings]])
            (compojure [route :as route]
                       [core :refer :all])
            (ring.middleware [params :refer [wrap-params]]
                             [anti-forgery :refer [wrap-anti-forgery]]
                             [gzip :refer [wrap-gzip]]
                             [stacktrace :refer [wrap-stacktrace]]
                             [format-params :refer [wrap-restful-params]]
                             [format-response :refer [wrap-restful-response]]
                             [keyword-params :refer [wrap-keyword-params]]
                             [nested-params :refer [wrap-nested-params]]
                             [multipart-params :refer [wrap-multipart-params]])
            [dieter.core :refer [asset-pipeline]]
            [org.httpkit.server :refer [run-server]]
            (noir [cookies :refer [wrap-noir-cookies]]
                  [session :refer [wrap-noir-session wrap-noir-flash]]
                  [validation :refer [wrap-noir-validation]])
            [noir.util.middleware :refer [wrap-request-map
                                          wrap-force-ssl
                                          wrap-strip-trailing-slash]]))

(defn attempt-model-connection []
  (try
    (model/connect storage)
    (catch java.io.IOException e
      (println "ERROR: Could not connect to MongoDB."))
    (catch java.lang.NullPointerException e
      (println "ERROR: Could not authenticate with Mongo. See config: \n\t"
               (str (assoc storage :password "**********"))))))

(def app
  "Create the application from its routes and middlewares."
  (-> app-routes
    (friend/authenticate friend-settings)
    (wrap-anti-forgery)
    (wrap-noir-validation)
    (wrap-restful-response)
    (wrap-restful-params)
    (wrap-multipart-params)
    (wrap-keyword-params)
    (wrap-nested-params)
    (wrap-params)
    (wrap-strip-trailing-slash)
    (wrap-request-map)
    (wrap-noir-cookies)
    (wrap-noir-flash)
    (wrap-noir-session
      {:store (cookie-store {:key (read-config "SECRET_KEY")})})))

(defn -main
  "Run the application."
  ([options]
   (state/compute)
   (attempt-model-connection)
   (let [assets-config {:cache-mode (state/get :mode)
                        :engine (if (state/production?) :rhino :v8)
                        :compress (state/production?)}]
     (run-server (asset-pipeline (if (state/production?)
                                   (-> app (wrap-force-ssl))
                                   (-> app (wrap-stacktrace)))
                                 assets-config)
                 {:port (state/get :port)})))
  ([] (-main {})))

(defn defserver
  "Start a server and bind the result to a var, 'server'."
  [& args]
  (def server (apply -main args)))
