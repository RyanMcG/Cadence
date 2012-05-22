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
    [:h2 "Let's get down to business!"]
    [:div#train_well.well.container-fluid
     [:p.help "Simply type the following phrase in the form repeatedly until I"
      " tell you to stop."]
     (let [phrase (model/get-phrase)]
       (html [:div.row-fluid
              [:div#training-phrase.input-xlarge.uneditable-input.span12
               phrase]]
             [:form#trainer.row-fluid
              (common/input {:type "text"
                             :eclass ".phrase.input-xlarge.span12"
                             :name "phrase"
                             :placeholder phrase})]))
     [:div#feedback.row-fluid]
     [:div#completion.row-fluid
      (let [trcount (count (patrec/kept-cadences))]
        (html
          [:div.progress.progress-success.progress-striped.span10
           [:div.bar
            {:style (str "width: "
                         (* 100.0 (/ trcount @patrec/training-min))
                         "%;")}]]
          (if (>= trcount @patrec/training-min)
            [:button.btn.btn-large.btn-success.span2 "I'm done"]
            [:button.btn.btn-large.disabled.span2 "Complete"])))]]))

(defpage post-training [:post "/user/training"] {:as unkeyed-cad}
  ; I like maps with keyword keys
  (let [cadence (keywordize-keys unkeyed-cad)]
    ; Ensure that the supplied data is really what I want it to be and not some
    ; glitch or fabrication. (This uses noir.validation)
    (if (is-valid/cadence? cadence)
      (if (patrec/keep-cadence cadence)
        (let [trnmin @patrec/training-min
              kept (patrec/kept-cadences)
              trcount (count kept)
              done (<= trnmin trcount)]
          (resp/json {:success true
                      :done done
                      :session kept
                      :progress (* 100.0 (/ trcount @patrec/training-min))}))
        (resp/json {:success true
                    :done false
                    :progress 0}))
      ; If bad data was received then tell the client we were not successful
      (resp/json {:success false
                  ; Grab errors put on the cadence field using noir.validation
                  :errors (vali/get-errors :cadence)
                  :progress 0}))))
