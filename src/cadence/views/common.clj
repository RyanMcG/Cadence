(ns cadence.views.common
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpartial base-layout [& content]
            (html5
              [:head
               [:title "Cadence"]
               ; Meta Tag Necessary for Twitter Boostrap
               [:meta {:name "viewport"
                       :content "width=device-width, initial-scale=1.0"}]
               ; Get jQuery
               (include-css "/css/bootstrap.min.css")
               (include-js (str "https://ajax.googleapis.com/ajax/libs/"
                                "jquery/1.7.2/jquery.min.js")
                           "/js/jquery-1.7.2.min.js")]
              [:body
               content]
              (include-js "/js/bootstrap.min.js")
              (include-js "/js/cadence.js")))

(defpartial user-links []
            [:li.dropdown
             [:a.dropdown-toggle
              {:data-toggle "dropdown"}
              [:i.icon-user.icon-white] " User " [:b.caret]]
             [:ul.dropdown-menu
              [:li [:a {:href "/signup"} [:i.icon-check] " Sign Up"]]
              [:li [:a {:href "/login"} [:i.icon-share] " Log In"]]]])

(defpartial layout [& content]
            (base-layout
              [:div#navbar.navbar.navbar-fixed-top
               [:div.navbar-inner
                [:div.container
                 [:a.btn.btn-navbar
                  {:data-toggle "collapse" :data-target ".nav-collapse"}
                  [:span.icon-bar] [:span.icon-bar] [:span.icon-bar]]
                  [:a.brand.dropdown-toggle {:href "#"} "Cadence"]
                  [:div.nav-collapse
                   [:ul.nav
                    [:li.active [:a {:href "/"}
                                 [:i.icon-home.icon-white] " Home"]]
                    [:li [:a {:href "/about"} "About"]]]
                   [:ul.nav.pull-right
                    [:li.divider-vertical]
                    (user-links)]
                   ]]]]
               [:div#main-wrapper
                [:div#main.container content]]))
