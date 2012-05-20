(ns cadence.views.landing
  (:require [cadence.views.common :as common]
            [cadence.model :as model]
            [cadence.model.recaptcha :as recaptcha]
            [cadence.model.validators :as is-valid]
            [cadence.model.flash :as flash]
            [cemerick.friend :as friend]
            [noir.response :as resp])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpage root "/" []
  (common/layout
    [:div.page-header [:h1 "Cadence"]]
    [:p "Welcome to cadence"]))

(defpage login "/login" []
  (common/layout
    [:div.page-header [:h1 "Login"]]
    (common/default-form
      :#login.well.form-inline
      {:action "/login" :method "POST"}
      [{:type "username" :name "Username"}
       {:type "password" :name "Password"}]
      [{:value "Log In"}])))

(defn- clear-identity [response]
  (update-in response [:session] dissoc ::identity))

(defpage logout "/logout" []
  (flash/put! :success "You have been logged out.")
  (clear-identity (resp/redirect "/")))

(defpage signup "/signup" {:as user}
  (common/layout
    [:div.page-header [:h1 "Sign Up"]]
    ; Make the recaptcha theme 'clean'
    [:script "var RecaptchaOptions = { theme : 'clean'}"]
    (common/control-group-form
      :#login.well.form-horizontal
      {:action "/signup" :method "POST"}
      [{:type "username" :name "Username" :required "yes"
        :value (escape-html (get user :username))}
       {:type "text" :name "Name" :placeholder "Optional"
        :value (escape-html (get user :name))}
       {:type "email" :name "Email" :placeholder "Optional"
        :value (escape-html (get user :email))}
       {:type "password" :name "Password" :required "yes"
        :value (get user :password)}
       {:type "password" :name "Repeat Password"
        :required "yes"}
       {:type "custom" :name "Humans only"
        :content (recaptcha/get-html
                   (escape-html
                     (get user :errors)))}]
      [{:eclass :.btn-primary
        :value "Sign Up"}])))

(defpage signup-check [:post "/signup"] {:as user}
  (if (is-valid/user? user)
    (do
      (model/add-user user)
      (flash/put! :success "Successfully Signed Up!")
      (resp/redirect (url-for root)))
    (do
      (flash/put! :error "Sorry, but your input has some validation errors.")
      (render signup user))))

(defpage about "/about" []
  (common/layout
    [:div.page-header [:h1 "About"]]
    [:p "derpderpderp"]))
