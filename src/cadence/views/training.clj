(ns cadence.views.training
  (:require [cadence.views.common :as common]
            [cadence.model :as model]
            (noir [response :as resp]
                  [session :as sess]))
  (:use noir.core
        [hiccup.core :exclude [as-str]]
        hiccup.page-helpers))

(defpage training "/training" []
  (common/layout
    [:div.page-header
     [:h1 "Training"]]
    [:p "If you don't know what this is for please checkout the "
     (link-to "/#training" "blurb on the front page") "."]
    [:h2 "Let's get to training!"]
    [:div.well
     [:p.help "Simply type the following phrase in the form repeatedly until I
              tell you to stop."]
     [:h3.training-phrase (model/get-phrase)]
     (sess/put! :train-count 0)
     (:form:#trainer
       (common/input {:type "text"
                      :name (str "phrase-"
                                 (sess/get :train-count))
                      :placeholder ""}))]))

(defpage post-training [:post "/training"] {:keys [cadence]}
  (resp/json cadence))
