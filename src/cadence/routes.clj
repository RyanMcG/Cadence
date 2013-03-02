(ns cadence.routes
  (:require [noir.response :as response]
            (cemerick [friend :as friend]
                      [drawbridge :as drawbridge])
            (compojure [route :as route] [core :refer :all])
            [cadence.security :refer [wrap-anonymous-only]]
            (cadence.views [response-codes :as response-codes]
                           [landing :as views-landing]
                           [training :as views-training]
                           [user :as views-user])))

(defroutes admin-routes
  (let [nrepl-handler (drawbridge/ring-handler)]
    (ANY "/repl" [] nrepl-handler)))

(defroutes user-routes
  (GET ["/auth/as/:crypt-user-id" :crypt-user-id #"^[\da-fA-F]{10,40}$"]
       [] views-training/auth)
  (POST ["/auth/as/:crypt-user-id" :crypt-user-id #"^[\da-fA-F]{10,40}$"]
        [] views-training/auth-check)
  (GET "/auth" [] views-training/auth)
  (POST "/auth" [] views-training/auth-check)
  (GET "/training" [] views-training/training)
  (POST "/training" [] views-training/training-post)
  (GET "/profile/:username" [] views-user/profile)
  (GET "/profile" [] views-user/profile)
  (ANY "/logout" [] views-user/logout))

(defroutes app-routes
  (GET "/" [] views-landing/root)
  (context "/user" [] (friend/wrap-authorize user-routes
                                             #{:cadence.security/user}))
  (context "/admin" [] (friend/wrap-authorize admin-routes
                                              #{:cadence.security/admin}))
  (GET "/login" [] (wrap-anonymous-only views-user/login
                                        "You must "
                                        [:a {:href "/user/logout"} "logout"]
                                        " before you can log in again."))
  (GET "/signup" [] (wrap-anonymous-only views-user/signup
                                  "You must "
                                  [:a {:href "/user/logout"}
                                   "logout"]
                                  " before creating an account."))
  (POST "/signup" [] (wrap-anonymous-only views-user/signup-check
                                  "You must "
                                  [:a {:href "/user/logout"}
                                   "logout"]
                                  " before creating an account."))
  (route/resources "/")
  (ANY ["/doc:anything" :anything #"^(?!s/index.html).*$"] []
       (response/redirect "/docs/index.html"))
  (route/not-found response-codes/not-found))
