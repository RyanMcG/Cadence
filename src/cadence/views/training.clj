(ns cadence.views.training
  (:require [cadence.views.common :as common]
            [cadence.model :as model]
            [cadence.model.flash :as flash]
            [cadence.pattern-recognition :as patrec]
            [cadence.model.validators :as is-valid]
            [noir.validation :as vali]
            (noir [response :as resp]
                  [session :as sess]))
  (:use noir.core
        clojure.walk
        hiccup.core
        hiccup.page-helpers))

(defpage training "/user/training" []
  (common/with-javascripts (concat common/*javascripts* ["/js/cadence.js"
                                                         "/js/runner.js"])
    (common/layout
      [:div.page-header
       [:h1 "Training"]]
      [:p "If you don't know what this is for please checkout the "
       (link-to "/#training" "blurb on the front page") "."]
      (if-let [phrase-doc (or (sess/get :training-phrase)
                              (get
                                (sess/put! :possible-training-phrase
                                           (model/get-phrase (:_id
                                                               (model/get-auth))
                                                             false))
                                "possible-training-phrase"))]
        ; Found a training phrase in the session or grabbed a new one from the
        ; database.
        (let [phrase (:phrase phrase-doc)]
          (html
            [:h2 "Let's get down to business!"]
            [:div#train_well.well.container-fluid
             [:p.help "Simply type the following phrase in the form repeatedly
                      until I tell you to stop."]
             ; Show a phrase to type in and an input field to for the user to
             ; copy it.
             (common/phrase-fields "trainer" phrase)
             [:div#feedback.row-fluid]
             [:div#completion.row-fluid
              (let [trcount (count (patrec/kept-cadences))]
                (html
                  [:div
                   {:class (str "progress progress-success progress-striped
                                span10"
                                (when (>= trcount @patrec/training-min)
                                  " active"))}
                   [:div.bar
                    {:style (str "width: "
                                 (min 100
                                      (* 100.0
                                         (/ trcount @patrec/training-min)))
                                 "%;")}]]
                  (if (>= trcount @patrec/training-min)
                    [:div.span2 [:a.btn.btn-large.btn-success
                                 {:href "/user/profile"} "Already done!"]]
                    [:div.span2 [:a.btn.btn-large.disabled
                                 {:href "#"} "Complete"]])))]]))
        ; If there is no training-phrase in the session and the current user has
        ; no untrained phrases then the user cannot do more training.
        (common/alert :info [:h2 "You are already trained!"]
                      (html [:p "There is a limited number of phrases and you
                                have done the training for all of them."]
                            [:p "Currently, there is no support for redoing your
                                training for a given phrase, but hopefully there
                                will be soon."]) false)))))

(defpage post-training [:post "/user/training"] {:as unkeyed-cad}
  ; I like maps with keyword keys
  (let [cadence (keywordize-keys unkeyed-cad)]
    ; When training-phrase is unset, set it with the value of
    ; possible-training-phrase (set when /user/training is accessed via GET). If
    ; the client somehow manipulates this the worst case scenario is that
    ; :training-phrase will be null wich should be caught by the cadence?
    ; validator.
    (when (nil? (sess/get :training-phrase))
      (sess/put! :training-phrase (let [tp (sess/get :possible-training-phrase)]
                                    (sess/remove! :possible-training-phrase)
                                    tp)))
    ; Ensure that the supplied data is really what I want it to be and not some
    ; glitch or fabrication. (This uses noir.validation)
    (if (is-valid/cadence? cadence false)
      (if (patrec/keep-cadence cadence)
        (let [trnmin @patrec/training-min
              kept (patrec/kept-cadences)
              trcount (count kept)
              done (>= trcount trnmin)]
          (resp/json {:success true
                      :done done
                      :session kept
                      :progress (* 100.0 (/ trcount trnmin))}))
        (resp/json {:success true
                    :done false
                    :progress 0}))
      ; If bad data was received then tell the client we were not successful
      (resp/json {:success false
                  ; Grab errors put on the cadence field using noir.validation
                  :errors (vali/get-errors :cadence)
                  :progress 0}))))

(defpage auth "/user/auth" {:keys [as-user]}
  (common/with-javascripts (concat common/*javascripts* ["/js/cadence.js"
                                                         "/js/runner.js"])
    (common/layout
      (let [phrase-doc (model/get-phrase (:_id (model/get-auth)) true)
            phrase (:phrase phrase-doc)]
        (sess/put! :auth-phrase phrase-doc)
        (html
          [:div.page-header [:h1 "Authenticate"]]
          [:p "If you don't know what this is for please checkout the "
           (link-to "/#authentication" "blurb on the front page") "."]
          [:h3 "Authenticating as " (or as-user "yourself")]
          [:div#auth_well.well.container-fluid
           (common/phrase-fields "authenticate" phrase)
           [:div#feedback.row-fluid]])))))

;; Take requests (probably ajax) and return json response defining whether the
;; server was successful and other related information.
(defpage auth-check [:post "/user/auth"] {:as unkeyed-cadence}
   (let [cadence (keywordize-keys unkeyed-cadence)]
     (if (is-valid/cadence? cadence true)
       (do
         (println "Classifying Authorization Input for " (model/identity))
         (let [classifier (time (model/get-classifier (:_id (model/get-auth))
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
                       :evaluation_keys (keys evaluation)
                       })))
       (resp/json {:success false
                   :type "error"
                   :errors (vali/get-errors :cadence)}))))

(defpage auth-as [:get "/user/auth/as/:crypt-user-id"
                  :crypt-user-id #"^[\da-fA-F]{10,40}$"]
  {:keys [crypt-user-id]}
  (render auth {:as-user crypt-user-id}))

(defpage auth-as-check [:post "/user/auth/as/:crypt-user-id"
                        :crypt-user-id #"^[\da-fA-F]{10,40}$"]
  {:as params}
  (render auth-check params))
