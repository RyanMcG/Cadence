(ns cadence.views.training
  (:require [cadence.views.common :as common]
            [cadence.model :as model]
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
  (common/layout
    [:div.page-header
     [:h1 "Training"]]
    [:p "If you don't know what this is for please checkout the "
     (link-to "/#training" "blurb on the front page") "."]
    (if-let [phrase-doc (or (sess/get :training-phrase)
                            (sess/put! :possible-training-phrase
                                       (model/get-phrase (:_id (model/get-auth))
                                                         false)))]
      ; Found a training phrase in the session or grabbed a new one from the
      ; database.
      (let [phrase (:phrase phrase-doc)]
        (html
          [:h2 "Let's get down to business!"]
          [:div#train_well.well.container-fluid
           [:p.help "Simply type the following phrase in the form repeatedly
                    until I tell you to stop."]
           ; Show a phrase to type in and an input field to for the user to copy
           ; it.
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
                              will be soon."]) false))))

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
    (if (is-valid/cadence? cadence)
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
