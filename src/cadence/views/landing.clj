(ns cadence.views.landing
  (:require [cadence.views.common :as common])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpage root "/" []
  (common/layout
    [:div.page-header [:h1 "Welcome to Cadence!"]]
    [:container-fluid
     [:div.row-fluid
      [:div.span3
       [:ul.nav.nav-stacked.nav-tabs
        [:li (link-to "#intro" "A Quick Introduction")]
        [:li (link-to "#usage" "What to do?")]
        [:li (link-to "#more" "More Info")]]]
      [:div.span9
       [:h2#intro "A Quick Introduction"]
       [:p "Cadence is really two things, a web app and "
        (link-to "https://github.com/RyanMcG/Cadence-js" "Cadence.js")
        ". The latter is a jQuery plugin which is used to monitor user input
        style on a single input field. For more information on "
        (link-to "https://github.com/RyanMcG/Cadence-js" "Cadence.js")
        " check out the links in the navbar."]
       [:p "This web app is a demo/example usage of Cadence.js. There are several
           things you need to do to test it out.  First off, you need to "
        (link-to "/signup" "signup") " and " (link-to "/login" "login") "."]]]]))
