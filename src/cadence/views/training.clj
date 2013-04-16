(ns cadence.views.training
  (:require [cadence.views.common :as common]
            [cadence.model :as model]
            [cadence.model.flash :as flash]
            [cadence.pattern-recognition :as patrec]
            [cadence.model.validators :as is-valid]
            [noir.validation :as vali]
            (noir [response :as resp]
                  [session :as sess]))
  (:use clojure.walk
        (hiccup def core page element)))


(defhtml training-well
  "Content/markup for the training page."
  [phrase done percent-complete]
  [:h2 "Let's get down to business!"]
  [:div#train_well.well.container-fluid
   [:p.help "Simply type the following phrase in the form repeatedly
            until I tell you to stop."]
   ; Show a phrase to type in and an input field to for the user to
   ; copy it.
   (common/phrase-fields "trainer" phrase)
   [:div#feedback.row-fluid]
   [:div#completion.row-fluid
    [:div
     {:class (str "progress progress-success progress-striped
                  span10" (when done " active"))}
     [:div.bar
      {:style (str "width: " percent-complete "%;")}]]
    [:div.span2 [:a {:href "#" :class (str "btn btn-large disabled"
                                           (when done " btn-success"))}
                 "Complete"]]]])

(defn training
  "Determine what phrase the current user should train on and display some
  elements to input the phrase and see some feedback and progress."
  [request]
  (common/layout
    [:div.page-header
     [:h1 "Training"]]
    [:p "If you don't know what this is for please checkout the "
     (link-to "/#training" "blurb on the front page") "."]
    (let [current-user-id (:_id (model/current-user))
          phrase-from-session (sess/get :training-phrase)
          phrase-doc (if (or (nil? phrase-from-session)
                             (model/phrase-complete-for-user?
                               (:_id phrase-from-session)
                               current-user-id))
                       (model/get-phrase-for-training current-user-id)
                       phrase-from-session)
          {phrase-id :_id phrase :phrase} phrase-doc
          training-count (model/count-cadences phrase-id current-user-id)
          training-min @patrec/training-min
          done (>= training-count training-min)
          percent-complete (min 100 (* 100.0 (/ training-count training-min)))]
      (sess/put! :training-phrase phrase-doc)
      (training-well phrase done percent-complete))))

(defn training-post
  "Add the posted cadence to the current user if it is valid and helps with
  training."
  [{unkeyed-cad :body-params}]
  (let [cadence (keywordize-keys unkeyed-cad)
        phrase-doc (sess/get :training-phrase)]
    (if (is-valid/cadence-for? :training cadence)
      (let [training-min @patrec/training-min
            kept-count (model/keep-cadence (:_id phrase-doc)
                                           (:_id (model/current-user))
                                           cadence)]
        (resp/json {:done (>= kept-count training-min)
                    :progress (* 100.0 (/ kept-count training-min))}))
      (resp/status 400 (resp/json {:errors (vali/get-errors :cadence)})))))

(defn auth [{:keys [as-user]}]
  (common/layout
    (let [phrase-doc (model/get-phrase-for-auth (:_id (model/get-auth)))
          phrase (:phrase phrase-doc)]
      (sess/put! :auth-phrase phrase-doc)
      (html
        [:div.page-header [:h1 "Authenticate"]]
        [:p "If you don't know what this is for please checkout the "
         (link-to "/#authentication" "blurb on the front page") "."]
        [:h3 "Authenticating as " (or as-user "yourself")]
        [:div#auth_well.well.container-fluid
         (common/phrase-fields "authenticate" phrase)
         [:div#feedback.row-fluid]]))))

(defn auth-check [{unkeyed-cadence :body-params :as request}]
  "Take requests (probably ajax) and return json response defining whether the
  server was successful and other related information."
  (let [cadence (keywordize-keys unkeyed-cadence)]
    (if (is-valid/cadence-for? :auth cadence)
      (do
        (println "Classifying Authorization Input for " (model/identity))
        (let [classifier (time (model/get-classifier (:_id (model/current-user))
                                                     (:phrase cadence)))
              result (patrec/classify-cadence classifier cadence)
              authenticated (= :good result)
              evaluation (time (patrec/evaluate-classifier classifier))
              error-rate (:error-rate evaluation)]
          (resp/json {:success true
                      :result result
                      :type (if authenticated "success" "warning")
                      :message (if authenticated
                                 "You successfully authenticated!"
                                 "You did <strong>not</strong> authenticate as
                                 the given user.")
                      :evaluation (select-keys evaluation [:false-negative-rate
                                                           :false-positive-rate
                                                           :error-rate
                                                           :precision])
                      ;:usingRbf (.getUseRBF (:classifier classifier))
                      :classifierOptions (patrec/interpret-classifier-options
                                           (seq (.getOptions (:classifier
                                                               classifier))))
                      :evaluation_keys (keys evaluation)})))
      (resp/json {:success false
                  :type "error"
                  :errors (vali/get-errors :cadence)}))))
