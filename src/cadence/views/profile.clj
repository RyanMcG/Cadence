(ns cadence.views.profile
  (:require [cadence.views.common :as common]
            [cadence.model :as m]
            [cadence.model.flash :as flash]
            [cadence.pattern-recognition :as patrec]
            [noir.session :as sess]
            [noir.response :as resp])
  (:use noir.core
        clavatar.core
        hiccup.core
        hiccup.page-helpers))

(defpartial phrases []
  [:p "Phrases"])

(defpartial visualizer []
  [:p "Visualizer"])

(defpage user-profile "/user/profile/:username" {:keys [username]}
  (if (= username (m/identity))
    (common/with-javascripts (conj common/*javascripts* "/js/g.raphael-min.js")
      (common/layout
        (let [user (m/get-auth)]
          (html
            ; When there are a suffecient number of training cadences
            (when (<= @patrec/training-min (count (patrec/kept-cadences)))
              (let [user-id (:_id user)
                    phrase-id (:_id (sess/get :training-phrase))]
                ; Add cadences to mongo
                (m/add-cadences user-id phrase-id (sess/get :training-cadences))
                ; Add the current user-id to array of trained users on the given
                ; phrase
                (m/add-trained-user-to-phrase user-id phrase-id))
              ; Remove the cadenences from training stored in the client's
              ; session.
              (sess/remove! :training-cadences)
              (sess/remove! :training-phrase)
              ; Let the user know they've been a good minion ;-) .
              (common/alert :success "Congratulations!"
                            "You've sucessfully completed training!"))
            [:div.page-header.username [:h1 (h (or (:name user) username))]
             [:img {:src (gravatar (h (:email user))) :title (h username)}]]
            [:div#profile.container-fluid
             [:p (str user)]
             [:div.row-fluid
              [:div.span6
               (phrases)]
              [:div.span6
               (visualizer)]]]))))
    (do
      (flash/put! :error (str "You cannot access " username "'s page."))
      (resp/redirect (str "/user/profile/" (m/identity))))))

(defpage user-profile-default "/user/profile" []
  ; Simply forward /user/profile acesses to that of the signed in user.
  (resp/redirect (url-for user-profile {:username (m/identity)})))
