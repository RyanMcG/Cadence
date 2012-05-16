(ns cadence.views.common
  (:require [clojure.string :as string]
            [cemerick.friend :as friend]
            [cadence.model :as m]
            [cadence.model.flash :as flash])
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
     [:link {:rel "shortcut icon" :type "image/x-icon" :href "/favicon.ico"}]
     (include-css "/css/bootstrap.min.css")
     (include-js (str "https://ajax.googleapis.com/ajax/libs/"
                      "jquery/1.7.2/jquery.min.js")
                 "/js/jquery-1.7.2.min.js")]
    [:body
     content]
    (include-js "/js/bootstrap.min.js")
    (include-js "/js/cadence.js")
    (include-js "/js/form-listener.js")))

(defpartial user-links []
  [:li.dropdown
   (if (friend/anonymous?)
     (html [:a.dropdown-toggle
            {:data-toggle "dropdown"}
            [:i.icon-user.icon-white] " User " [:b.caret]]
           [:ul.dropdown-menu
            [:li [:a {:href "/signup"} [:i.icon-check] " Sign Up"]]
            [:li [:a {:href "/login"} [:i.icon-share] " Log In"]]])
     (html [:a.dropdown-toggle
            {:data-toggle "dropdown"}
            [:i.icon-user.icon-white] " " (m/identity) " " [:b.caret]]
           [:ul.dropdown-menu
            [:li [:a {:href "/logout"} [:i.icon-off] " Log Out"]]]))])

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
         [:li [:a {:href "/"}
               [:i.icon-home.icon-white] " Home"]]
         [:li [:a {:href "/about"} "About"]]]
        [:ul.nav.pull-right
         [:li.divider-vertical]
         (user-links)]
        ]]]]
    [:div#main-wrapper
     [:div#main.container
      (when-let [{:keys [type message]} (flash/get)]
        (let [type (name type)]
          [:div {:id "flash" :class (str "alert alert-" type)}
           [:a.close {:data-dismiss "alert"} "x"]
           [:strong (string/capitalize type) ": "] message]))
      content]]))

(defpartial control-group [params]
  (let [name (string/replace (string/lower-case (:name params)) #"\s" "-")]
    [:div.control-group
     [:label.control-label {:for name} (str (:name params) ": ")]
     [:div.controls
      [:input (assoc (dissoc params :more) :name name)]
      (:more params)]]))

(defn- as-css-id [s]
  (name (if (nil? s) "" s)))

(defpartial form-button [{:keys [eclass value]}]
  [(keyword (str "button.btn" (as-css-id eclass)))
   {:type "submit"} value])

(defpartial control-group-form [id+class params items buttons]
  [(keyword (str "form" (as-css-id id+class))) params
   [:fieldset
    (map control-group items)
    [:div.form-actions
     (map form-button buttons)]]])

(defpartial input [{:keys [eclass type name placeholder]}]
  [(keyword (str "input" (as-css-id eclass)))
   {:type type
    :name (string/lower-case name)
    :placeholder (or placeholder name)}])

(defpartial default-form [id+class params items buttons]
  [(keyword (str "form" (as-css-id id+class))) params
   (interpose " " (map input items)) " "
   (map form-button buttons)])
