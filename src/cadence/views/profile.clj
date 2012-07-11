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
        hiccup.page-helpers
        [clojure.pprint :only [pprint]]))

;; Displays information about a single phrase.
(defpartial phrase-row [index [phrase cads]]
  [:div.phrase-header.row-fluid {:class (str "phrase-header-" (if (even? index)
                                                                "even" "odd"))
                                 :data-toggle "collapse"
                                 :data-target (str "#phrase_body" index)}
   [:div.span6.phrase
    [:span.quote "&laquo;"]
    [:em.phrase phrase]
    [:span.quote "&raquo;"]]
   [:div.span3.cad-count (count cads)]
   [:div.span3.is-trained (if (:trained cads) "☑" "☒")]]
  [:div.phrase-body.collapse {:id (str "phrase_body" index)}
   [:div.well "DerpDerp"]])

;; Displays information about phrases.
(defpartial phrases [user-id]
  (let [phrases (m/get-user-phrase-stats user-id)]
    [:div.phrases
     [:div.phrase-headers.row-fluid
      [:div.span6 "Phrases"]
      [:div.span3 "# Cadences"]
      [:div.span3 "Trained?"]]
     [:div.phrases.accordian.row-fluid
      (apply str (loop [p phrases
                        l 1
                        t []]
                   (if-not (empty? p)
                     (recur (next p) (inc l) (conj t (phrase-row l (first p))))
                     t)))]]))

;; Defines the markup for the visualizer.
(defpartial visualizer []
  [:h2 "Visualizer"]
  [:div.visualizer "The visualizer."])

;; Defines the user profile page. When this page is visited after training has
;; been completed it saves the associated cadences for the user.
(defpage user-profile "/user/profile/:username" {:keys [username]}
  (if (= username (m/identity))
    (common/with-javascripts (conj common/*javascripts* "/js/g.raphael-min.js")
      (common/layout
        (let [user (m/get-auth)
              user-id (:_id user)]
          (html
            ; When there are a suffecient number of training cadences
            (when (<= @patrec/training-min (count (patrec/kept-cadences)))
              (let [phrase-id (:_id (sess/get :training-phrase))]
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
            [:div.page-header.row-fluid.username [:h1 (h (or (:name user) username))]
             [:img {:src (gravatar (h (:email user))) :title (h username)}]]
            [:div#profile
             [:div.row-fluid
              [:div.span6
               (phrases user-id)]
              [:div.span6
               (visualizer)]]]))))
    (do
      (flash/put! :error (str "You cannot access " username "'s page."))
      (resp/redirect (str "/user/profile/" (m/identity))))))

(defpage user-profile-default "/user/profile" []
  ; Simply forward /user/profile acesses to that of the signed in user.
  (resp/redirect (url-for user-profile {:username (m/identity)})))
