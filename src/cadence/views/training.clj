(ns cadence.views.training
  (:require [cadence.views.common :as common]
            [cadence.model :as model]
            (noir [response :as resp]
                  [session :as sess]))
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpage training "/user/training" []
  (common/layout
    [:div.page-header
     [:h1 "Training"]]
    [:p "If you don't know what this is for please checkout the "
     (link-to "/#training" "blurb on the front page") "."]
    [:h2 "Let's get to training!"]
    [:div#train_well.well.container-fluid
     [:p.help "Simply type the following phrase in the form repeatedly until I"
      " tell you to stop."]
     (let [train-count (sess/put! :train-count 0)
           phrase (model/get-phrase)]
       (html [:div.row-fluid
              [:div#training-phrase.input-xlarge.uneditable-input.span12
               phrase]]
             [:form#trainer.row-fluid
              (common/input {:type "text"
                             :eclass ".phrase.input-xlarge.span12"
                             :name (str "phrase-"
                                        (sess/get :train-count))
                             :placeholder phrase})]))
     [:div#feedback.row-fluid]
     [:div#completion.row-fluid
      [:h3.span2 "Completion: "]
      [:div.progress.progress-success.span10 [:div.bar {:style "width: 0%;"}]]]]))

(defpage post-training [:post "/training"] {:keys [cadence]}
  (resp/json cadence))
