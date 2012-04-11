(ns cadence.views.common
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpartial layout [& content]
            (html5
              [:head
               [:title "Cadence"]
               (include-css "/css/reset.css")
               (include-js "https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js")]
              [:body
               [:div#wrapper
                content]]
              (include-js "/js/bootstrap.js")))
