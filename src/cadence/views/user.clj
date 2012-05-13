(ns cadence.views.user
  (:require [cadence.views.common :as common]
            (noir response session))
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpage user "/user/{username}" {:keys [username]}
  (common/layout (str "Welcome " username "!")))
