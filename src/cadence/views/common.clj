(ns cadence.views.common
  (:require [clojure.string :as string]
            [cemerick.friend :as friend]
            [ring.util.anti-forgery :refer [anti-forgery-metas]]
            [cadence.model :as m]
            [dieter.core :refer [link-to-asset]]
            [cadence.security :refer [admin?]]
            [noir.validation :as vali]
            [cadence.model.flash :as flash])
  (:use (hiccup core def page)))

(defn base-layout [& content]
  (html5
    [:head
     [:title "Cadence"]
     [:link {:rel "shortcut icon" :type "image/x-icon" :href "/favicon.ico"}]
     (anti-forgery-metas)
     ; Meta Tag Necessary for Twitter Boostrap
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1.0"}]
     (include-css (link-to-asset "stylesheets/app.css"))
     (include-js (link-to-asset "javascripts/app.js"))
     [:script {:type "text/javascript"}
      "var _gaq = _gaq || [];
      _gaq.push(['_setAccount', 'UA-32354071-1']);
      _gaq.push(['_trackPageview']);

      (function() {
      var ga = document.createElement('script');
      ga.type = 'text/javascript'; ga.async = true;
      ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
      var s = document.getElementsByTagName('script')[0];
      s.parentNode.insertBefore(ga, s);
      })();"]]
    [:body content]))

(defhtml user-links []
  [:li.dropdown
   (if (friend/anonymous?)
     (html [:a.dropdown-toggle
            {:href "#" :data-toggle "dropdown"}
            [:i.icon-user.icon-white] " User " [:b.caret]]
           [:ul.dropdown-menu
            [:li [:a {:href "/signup"} [:i.icon-check] " Sign Up"]]
            [:li [:a {:href "/login"} [:i.icon-share] " Log In"]]])
     (html [:a.dropdown-toggle
            {:href "#" :data-toggle "dropdown"}
            [:i.icon-user.icon-white] " " (m/identity) " " [:b.caret]]
           [:ul.dropdown-menu
            [:li [:a {:href (str "/user/profile/" (m/identity))}
                  [:i.icon-th-list] " View Profile"]]
            [:li [:a {:href "/user/logout"} [:i.icon-off] " Log Out"]]]))])

(defn alert
  "Displays an alert box."
  ([class type message show-close?]
   [:div {:id "flash" :class (str "alert fade in alert-" (name class))}
    (when show-close?
      [:a.close {:data-dismiss "alert"} "&times;"])
    [:span.alert-label (if (keyword? type)
                         (string/capitalize (name type))
                         type) " "] message])
  ([class type message] (alert class type message true))
  ([type message] (alert type type message true)))

(defhtml admin-bar
  "A fixed bottom navbar that only appears for admins."
  []
  [:footer#admin-footer.footer.footer-sticky
   [:div#navbar.navbar
    [:div.navbar-inner
     [:div.container
      [:ul.nav
       [:li.title [:a {:href "#"} [:strong "ADMIN BAR"]]]
       [:li.divider-vertical]
       [:li [:a {:href "/admin/migrations"} "Migrations"]]]]]]])

(defn layout [& content]
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
           (html
             [:li [:a {:href "/user/training"} "Training"]]
             [:li [:a {:href "/user/auth"} "Auth"]]))
         [:li [:a {:href "/docs/index.html"} "Documentation"]]
         [:li [:a {:href "https://github.com/RyanMcG/Cadence"} "Source"]]
         [:li.dropdown
          [:a.dropdown-toggle
               {:href "#" :data-toggle "dropdown"} "Cadence.js " [:b.caret]]
          [:ul.dropdown-menu
           [:li [:a {:href "http://ryanmcg.github.com/Cadence-js"}
                 [:i.icon-book] " Documentation"]]
           [:li [:a {:href "https://github.com/RyanMcG/Cadence-js"}
                 [:i.icon-align-left] " Source"]]]]]
        [:ul.nav.pull-right
         [:li.divider-vertical]
         (user-links)]
        ]]]]
    [:div#main-wrapper
     [:div#main.container
      (when-let [{t :type c :class m :message} (flash/get)]
        (alert (if (nil? c) t c) t m))
      content
      [:footer#footer.footer
       [:a.label.label-inverse {:href "/#privacy"} "Privacy"]
       [:a.label {:href "/#security"} "Security"]
       [:a.label.label-success {:href "/#contact"} "Contact"]
       " &copy; 2012 Ryan McGowan"]
      (when (admin?) (admin-bar))]]))

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

(defn control-groupify
  "Allow the field to pass through unaltered unless it is a map then call
  control-group."
  [field]
  (let [field-isa? (partial isa? (type field))]
    ((cond
       (field-isa? clojure.lang.MapEquivalence) control-group
       :else identity) field)))

(defn- as-css-id [s]
  (name (if (nil? s) "" s)))

(defhtml form-button [{:keys [eclass value]}]
  [(keyword (str "button.btn" (as-css-id eclass)))
   {:type "submit"} value])

(defhtml control-group-form [id+class params items buttons]
  [(keyword (str "form" (as-css-id id+class))) params
   [:fieldset
    (map control-groupify items)
    [:div.form-actions
     (map form-button buttons)]]])

(defhtml map-to-input [{:keys [eclass type name placeholder params]}]
  [(keyword (str "input" (as-css-id eclass)))
   (merge params {:type type
                  :name (string/lower-case name)
                  :placeholder (or placeholder name)})])

(defn inputify
  "If field is a string then let it pass through, if it is a map then call
   map-to-input on it."
  [field]
  (let [field-isa? (partial isa? (type field))]
    ((cond
       (field-isa? clojure.lang.MapEquivalence) map-to-input
       :else identity) field)))

(defhtml default-form [id+class params items buttons]
  [(keyword (str "form" (as-css-id id+class))) params
   (interpose " " (map inputify items)) " "
   (map form-button buttons)])

(defhtml phrase-fields [id phrase]
  [:div.row-fluid
   [:div#given-phrase.input-xlarge.uneditable-input.span12
    phrase]]
  [:form.row-fluid {:id id}
   (map-to-input {:type "text"
                  :eclass ".phrase.input-xlarge.span12"
                  :name "phrase"
                  :placeholder phrase})])
