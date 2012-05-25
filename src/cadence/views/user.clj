(ns cadence.views.user
  (:require [cadence.views.common :as common]
            [cadence.model :as m]
            [cadence.model.flash :as flash]
            [cadence.model.recaptcha :as recaptcha]
            [cadence.model.validators :as is-valid]
            [cadence.pattern-recognition :as patrec]
            [noir.validation :as vali]
            [noir.session :as sess]
            [noir.response :as resp])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpage user-profile "/user/profile/:username" {:keys [username]}
  (if (= username (m/identity))
    (common/layout
      ; When there are a suffecient number of training cadences adn the
      (when (<= @patrec/training-min (count (patrec/kept-cadences)))
        (let [user-id (:_id (m/get-auth))
              phrase-id (:_id (sess/get :training-phrase))]
          ; Add cadences to mongo
          (m/add-cadences user-id phrase-id (sess/get :training-cadences))
          ; Add the current user-id to array of trained users on the given
          ; phrase
          (m/add-trained-user-to-phrase user-id phrase-id))
        ; Remove the cadenences from training stored in the client's session.
        (sess/remove! :training-cadences)
        ; Let the user know they've been a good minion ;-).
        (common/alert :success "Congratulations!"
                      "You've sucessfully completed training!"))
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
      (flash/put! :error (str "You cannot access " username "'s page."))
      (resp/redirect (str "/user/" (m/identity))))))

(defpage user-profile-default "/user/profile" []
  ; Simply forward /user/profile acesses to that of the signed in user.
  (resp/redirect (url-for user-profile {:username (m/identity)})))

; ----
;  ## User Signup and Login

(defpage login "/login" {:keys [login_failed username]}
  ; Defines a simple inline form. Friend does all of the work.
  (common/layout
    [:div.page-header [:h1 "Login"]]
    (common/default-form
      :#login.well.form-inline
      {:action "/login" :method "POST"}
      [{:type "username" :name "Username" :params {:value username}}
       {:type "password" :name "Password"}]
      [{:value "Log In"}])
    (when (= login_failed "Y")
      (common/alert :error "Sorry!" "You used a bad username/password."))))

(defn- clear-identity
  "Removes authentication related `::identity` from the session data."
  [response]
  ; Shamelessly stolen from friend (it's defined privately there)
  (update-in response [:session] dissoc ::identity))

(defpage logout "/logout" []
  (flash/put! :success "You have been logged out.")
  ; Calls `clear-identity` on the response to remove authentication information
  ; from the session.
  (clear-identity (resp/redirect "/")))

(defpage signup "/signup" {:as user}
  ; A nice signup page with validation.
  (common/layout
    [:div.page-header [:h1 "Sign Up"]]
    ; Make the recaptcha theme 'clean'
    [:script "var RecaptchaOptions = { theme : 'clean' };"]
    (common/control-group-form
      :#login.well.form-horizontal
      {:action "/signup" :method "POST"}
      [{:type "username" :name "Username" :required "yes"
        ; `esacape-html` on user input to avoid XSS.
        :value (escape-html (get user :username))}
       {:type "text" :name "Name" :placeholder "Optional"
        :value (escape-html (get user :name))}
       {:type "email" :name "Email" :placeholder "Optional"
        :value (escape-html (get user :email))}
       {:type "password" :name "Password" :required "yes"
        :value (get user :password)}
       {:type "password" :name "Repeat Password"
        :required "yes"}
       ; Uses the special case `:type "custom"` to use any html as the form.
       ; Here there purpose is to add a captcha.
       {:type "custom" :name "Humans only"
        :content (recaptcha/get-html
                   (escape-html
                     (get user :errors)))}]
      [{:eclass :.btn-primary
        :value "Sign Up"}])))

(defpage signup-check [:post "/signup"] {:as user}
  ; Validate user signup info and add them to mongo on success.
  (if (is-valid/user? user)
    (try
      ; Here we try adding a user
      (m/add-user
        (select-keys user [:username :name :email :password]))
      (flash/put! :success "You've signed up! Now try and login.")
      (resp/redirect (url-for login))
      (catch com.mongodb.MongoException e
        ; `username` is a unique key so if there's a mongo exception it's
        ; probably a duplicate username.
        (flash/put! :error "Error: " "Sorry, that username is already in use.")
        ; Manually set an error on the username field.
        (vali/set-error :username "Please try a different username.")
        ; Render the signup page so the client can try a different username.
        (render signup user)))
    (do
      ; Bas user input? Let them know
      (flash/put! :error "Sorry, but your input has some validation errors.")
      ; and then load the page again.
      (render signup user))))
