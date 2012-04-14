(ns cadence.views.common
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        cadence.views.landing))

(defpartial base-layout [& content]
            (html5
              [:head
               [:title "Cadence"]
               ; Necessary for Twitter Boostrap
               [:meta {:name "viewport"
                       :content "width=device-width, initial-scale=1.0"}]
               ; TODO Add Twitter boostrap
               ; TODO Remove this?
               (include-css "/css/reset.css")
               ; TODO Remove this?
               (include-js
                       "https://ajax.googleapis.com/ajax/libs/"
                       "jquery/1.7.2/jquery.min.js")]
              [:body
               content]
              (include-js "/js/bootstrap.js")))

(defpartial user-links []
            [:a {:href (url-for signup)} "login / sign up"])

(defpartial layout [& content]
            (base-layout
              [:header
               [:div.wrapper
                [:img#logo {:src "/img/cadence.png"}]
                [:div#title "Cadence"]
                [:div#user-links (user-links)]]]
              [:div#main.wrapper content]
              [:footer
               [:div.wrapper [:div.section
                              [:ul
                               [:li [:a {:href (url-for about)} "About"]]
                               ]]]]))


