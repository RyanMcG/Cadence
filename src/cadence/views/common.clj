(ns cadence.views.common
  (:require [clojure.string :as string]
            [cemerick.friend :as friend]
            [cadence.model :as m]
            [noir.validation :as vali]
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
    (include-js "/js/runner.js")))

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
            [:li [:a {:href (str "/user/profile/" (m/identity))}
                  [:i.icon-th-list] " View Profile"]]
            [:li [:a {:href "/logout"} [:i.icon-off] " Log Out"]]]))])

(defn alert
  "Displays an alert box."
  ([class type message show-close?] [:div {:id "flash" :class (str "alert fade in alert-" (name class))}
                                     (when show-close?
                                       [:a.close {:data-dismiss "alert"} "&times;"])
                                     [:strong (if (keyword? type)
                                                (string/capitalize (name type))
                                                type) " "] message])
  ([class type message] (alert class type message true))
  ([type message] (alert type type message true)))

(defpartial layout [& content]
  (base-layout
    [:div#navbar.navbar.navbar-fixed-top
     [:div.navbar-inner
      [:div.container
       [:a.btn.btn-navbar
        {:data-toggle "collapse" :data-target ".nav-collapse"}
        [:span.icon-bar] [:span.icon-bar] [:span.icon-bar]]
       [:a.brand.dropdown-toggle {:href "/"} "Cadence"]
       [:div.nav-collapse
        [:ul.nav
         [:li.divider-vertical]
         (when (not (friend/anonymous?))
           [:li
            [:a {:href "/user/training"} "Training"]])
         [:li [:a {:href "/about"} "About"]]]
        [:ul.nav.pull-right
         [:li.divider-vertical]
         (user-links)]
        ]]]]
    [:div#main-wrapper
     [:div#main.container
      (when-let [{t :type c :class m :message} (flash/get)]
        (println (name t))
        (alert (if (nil? c) t c) t m))
      content]]))

(defn format-errors
  "Takes a collection of error messages and formats it into html."
  [errs]
  (if (> (count errs) 1)
    [:div.help-block
     [:ul (map #(html [:li %]) errs)]]
    [:span.help-inline
     (first errs)]))

(defn control-group
  "Renders a single form element in a control-group."
  [params]
  (let [name (string/replace (string/lower-case (:name params))
                             #"\s" "-")
        field (keyword name)
        errors (vali/get-errors field)]
    [:div {:class (str "control-group" (when (not (empty? errors)) " error"))}
     [:label.control-label {:for name} (str (:name params) ": ")]
     [:div.controls
      (if (= (:type params) "custom")
        (:content params)
        [:input (assoc (dissoc params :more) :name name)])
      (when (not (nil? errors))
        (format-errors errors))
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

(defpartial input [{:keys [eclass type name placeholder params]}]
  [(keyword (str "input" (as-css-id eclass)))
   (merge params {:type type
    :name (string/lower-case name)
    :placeholder (or placeholder name)})])

(defpartial default-form [id+class params items buttons]
  [(keyword (str "form" (as-css-id id+class))) params
   (interpose " " (map input items)) " "
   (map form-button buttons)])
