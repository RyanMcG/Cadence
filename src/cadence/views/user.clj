(ns cadence.views.user
  (:require [cadence.views.common :as common]
            [cadence.model :as m]
            [cadence.model.flash :as flash]
            [cadence.model.recaptcha :as recaptcha]
            [cadence.model.validators :as is-valid]
            [cadence.pattern-recognition :as patrec]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [noir.validation :as vali]
            [noir.session :as sess]
            [noir.response :as resp])
  (:use (hiccup core page def element util)))

(defn profile [{{:keys [username]} :route-params :as request}]
  (if (= username (m/identity))
    (common/layout
      [:div.page-header [:h1 username]]
      [:div.container-fluid
       [:div.row-fluid
        [:div.span12
         ; No real content currently so let's just ignore this.
         [:div.hero-unit
          [:h2 "Hello there!"
           [:p "Unfortunately, your profile is pretty boring right now."
            " This should change in the near future."]]]]]])
    (do
      (flash/put! :error "You cannot access " username "'s page.")
      (resp/redirect (str "/user/profile/" (m/identity))))))

(defn profile-base [request]
  ; Simply forward /user/profile acesses to that of the signed in user.
  (resp/redirect (str "/user/profile/" (m/identity))))

; ----
;  ## User Signup and Login

(defn login [{:keys [login-failed username]}]
  ; Defines a simple inline form. Friend does all of the work.
  (common/layout
    [:div.page-header [:h1 "Login"]]
    (common/default-form
      :#login.well.form-inline
      {:action "/login" :method "POST"}
      [{:type "username" :name "Username" :params {:value username}}
       {:type "password" :name "Password"}
       (anti-forgery-field)]
      [{:value "Log In"}])
    (when (= login-failed "Y")
      (common/alert :error "Sorry!" "You used a bad username/password."))))

(defn- clear-session
  "Entirely overwrite the session in the response."
  [response]
  (assoc response :session nil))

(defn logout [request]
  (flash/put! :success "You have been logged out.")
  (clear-session (resp/redirect "/")))

(defn signup
  "A nice signup page with validation."
  [user]
  (common/layout
    [:div.page-header [:h1 "Sign Up"]]
    ; Make the recaptcha theme 'clean'
    [:script "var RecaptchaOptions = { theme : 'clean' };"]
    (common/control-group-form
      :#login.well.form-horizontal
      {:action "/signup" :method "POST"}
      [{:type "username" :name "Username" :required "yes"
        ; `esacape-html` on user input to avoid XSS.
        :value (escape-html (:usernam user))}
       {:type "text" :name "Name" :placeholder "Optional"
        :value (escape-html (:name user))}
       {:type "email" :name "Email" :placeholder "Optional"
        :value (escape-html (:email user))}
       {:type "password" :name "Password" :required "yes"
        :value (:password user)}
       {:type "password" :name "Repeat Password"
        :required "yes"}
       (anti-forgery-field)
       ; Uses the special case `:type "custom"` to use any html as the form.
       ; Here there purpose is to add a captcha.
       {:type "custom"
        :name "Humans only"
        :content (recaptcha/get-html
                   (escape-html (get user :errors)))}]
      [{:eclass :.btn-primary
        :value "Sign Up"}])))

(defn signup-check [{user :params}]
  ; Validate user signup info and add them to mongo on success.
  (if (is-valid/user? user)
    (try
      ; Here we try adding a user
      (m/add-user (select-keys user [:username :name :email :password]))
      (flash/put! :success "You've signed up! Now try and login.")
      (resp/redirect "/login")
      (catch com.mongodb.MongoException e
        ; `username` is a unique key so if there's a mongo exception it's
        ; probably a duplicate username.
        (flash/now! :error "Error: " "Sorry, that username is already in use.")
        ; Manually set an error on the username field.
        (vali/set-error :username "Please try a different username.")
        ; Render the signup page so the client can try a different username.
        (signup user)))
    (do
      ; Bad user input? Let them know
      (flash/now! :error "Sorry, but your input has some validation errors.")
      ; and then load the page again.
      (signup user))))
