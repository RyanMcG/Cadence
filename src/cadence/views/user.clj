(ns cadence.views.user
  (:require [cadence.views.common :as common]
            [cadence.model :as m]
            [cadence.model.flash :as flash]
            [cadence.model.recaptcha :as recaptcha]
            [cadence.model.validators :as is-valid]
            [cadence.pattern-recognition :as patrec]
            [cemerick.friend :as friend]
            [noir.validation :as vali]
            [noir.session :as sess]
            [noir.response :as resp])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpage user-profile "/user/profile/:username" {:keys [username]}
  (if (= username (m/identity))
    (common/layout
      (when (<= @patrec/training-min (count (patrec/kept-cadences)))
        (m/add-cadences (sess/get :training-cadences))
        (sess/remove! :training-cadences)
        (common/alert :success "Congratulations!"
                      "You've sucessfully completed training!"))
      [:div.page-header [:h1 username]]
      [:div.container-fluid
       [:div.row-fluid
        [:div.span12
         [:div.hero-unit
          [:h2 "Hello there!"
           [:p "Unfortunately, your profile is pretty boring right now."
            " This should change in the near future."]]]]]])
    (do
      (flash/put! :error (str "You cannot access " username "'s page."))
      (resp/redirect (str "/user/" (m/identity))))))

(defpage user-profile-default "/user/profile" []
  (resp/redirect (url-for user-profile {:username (m/identity)})))

(defpage login "/login" {:keys [login_failed username]}
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

(defn- clear-identity [response]
  (update-in response [:session] dissoc ::identity))

(defpage logout "/logout" []
  (flash/put! :success "You have been logged out.")
  (clear-identity (resp/redirect "/")))

(defpage signup "/signup" {:as user}
  (common/layout
    [:div.page-header [:h1 "Sign Up"]]
    ; Make the recaptcha theme 'clean'
    [:script "var RecaptchaOptions = { theme : 'clean' };"]
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
    (try
      (m/add-user
        (select-keys user [:username :name :email :password]))
      (flash/put! :success "You've signed up! Now try and login.")
      (resp/redirect (url-for login))
      (catch com.mongodb.MongoException e
        (flash/put! :error "Error: " "Sorry, that username is already in use.")
        (vali/set-error :username "Please try a different username.")
        (render signup user)))
    (do
      (flash/put! :error "Sorry, but your input has some validation errors.")
      (render signup user))))
