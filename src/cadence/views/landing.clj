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
      [:div#faqs.span2
       [:h3 "Questions"]
       [:ul.nav.nav-stacked.nav-tabs
        [:li (link-to "#intro" "What is all this?")]
        [:li (link-to "#usage" "How do I use this site?")]
        [:li (link-to "#privacy" "Can I trust you?")]
        [:li (link-to "#security" "Am I safe?")]
        [:li (link-to "#logo" "What's that cylinder thing?")]
        [:li (link-to "#contact" "I'm angry.  Who should I complain to?")]]]
      [:div.span10
       [:div.row-fluid
        [:div.span7
         [:h2#intro.anchor "A Quick Introduction"]
         [:p "Cadence is really two things, this web app and "
          (link-to "https://github.com/RyanMcG/Cadence-js" "Cadence.js")
          ".  The latter is a jQuery plugin which is used to monitor user input
          style on a single input field.  The site is demo of what I consider to
          be the primary use case for Cadence.js
          (and the reason for its creation), authentication."]
         [:p "Cadence authentication is a bit different than password
             authentication.  Typically you only have one password per site
          (sometimes fewer). You type this password in or your browser may fill
          it in for you, you click the \"login\" button and you're done.  Of
          course, you already know all that because you've been using passwords
          for most of your life. Theoretically, passwords work from a security
             standpoint.  However, they are often exploited since most users
          don't use them properly. For more on this (and the science behind
             cadence) see "
          (link-to "/cadence-paper.pdf" "my paper") "."]
         [:p "Instead of using passwords. Cadence asks the user to type in a
             given phrase. Using some pattern recognition algorithms the way the
             user typed in the given phrase is compared to past entries and
             classified as authentic or not. An authentic entry authenticates the
             user and an unauthentic entry requires he or she tries again."]
         [:p "You might be asking, what passed entries? What if this the first
             time the user types in a specific phrase? Well, that's what
             training is for.  In order to do authentication a classifier must
             be created and trained for that specific user.  During the training
             process the user is given a phrase and types it in until they are
             prompted to stop. Cadence.js is the tool that helps us with this.
             Once the classifier has been trained a user can use that phrase to
             authenticate."]
         [:p "See the "
          (link-to "#usage" "Using this site") " section to get started."]
         [:p " For more information on "
          (link-to "https://github.com/RyanMcG/Cadence-js" "Cadence.js")
          " check out the links in the purple bar at the top of the page."]]
        [:div.span5
         [:div.highlight
          [:h2 "Name & Logo"]
          [:p "The way we type a phrase, especially one we type a lot, often has
              a very specific rhythm.  In fact, that phrase is often reduced to
           that rhythm and set of hand movements in our mind.  It becomes a
           cadence that we perform every time we need to authenticate. We can
           tell when we've miss typed an often used password because we here
              that a note (i.e. key) was missing."]
          [:p "This is the essence of why Cadence works."]
          [:p#logo.anchor "The " [:strong "logo"] " is " [:em "supposed"]
           " to be a snare drum.  However, I am not a graphic designer.  If you
           think you can do better then please "
           (link-to "mailto:ryan@ryanmcg.com" "send me") " a SVG."]]]]
       [:h2#usage.anchor "Using this site"]
       [:p "There are a few things you things you can do with cadence, but they
           all require making an account first.  To sign up for an account "
        (link-to "/signup" "click here")
        " or use the links in User drop-down menu on the right side of the
        navigation bar.  Once you've signed up and logged in you should be able to start
        training." ]
       [:div.well
        [:p#training.anchor [:strong "What is training? "]
         "Training is an essential part of using supervised learners.  This
         application takes the output from Cadence.js during training and uses
         it to create a model for Support Vector Machine classification."]
        [:p#authentication.anchor "During " [:strong "authentication"]
         " that classifier is called up and used on a new output from Cadence.js
         and determines whether that output is \"authentic\".  Hopefully, for the
         classifier to consider the given cadence authentic it will have to be
         generated by whomever did the training in the first place."]
        [:div "On this site authentication does not get you anything special.
              This site is simply a demo of Cadence authentication.  To test it
              out you can try authentication as yourself, another anonymous,
              randomly selected user, or a user for whom you were given a
              private link."]] 
       [:div.row-fluid
        [:div.span6
         [:h3#privacy.anchor "Privacy"]
         [:p "In lieu of a long, complicated privacy policy I have this: "]
         [:div.well
          [:p [:strong "Your information"]
           " will not be given or sold to any third party.  During the sign up
           process this site asks for your name and email both of which are
           optional.  The name field is only used for display purposes
           (i.e. it's just to help make this site prettier).
            Your email is requested in the scenario that I need or want to send
           an announcement. It will be used sparingly if ever. If you want your
           email removed send an email to me at "
           (link-to "mailto:ryan@ryanmcg.com" "ryan@ryanmcg.com")
           " with the subject line \"Cadence Remove Email\"."]
          [:p [:strong "Your cadences"]
           " are also considered your information.  They are created during the
           training process and only used for your authentication and the
           authentication of other users.  When your cadences are being used to
           authenticate other users it is done anonymously."]]]
        [:div.span6
         [:h3#security.anchor "Security"]
         [:p "I take security very seriously. It would be bad practice to
             demonstrate a new method of authentication and make the demo a
             security risk. To help ensure the safety of you, the user, this
             site is "
          [:strong "only accessible over SSL."]]
         [:p
          "If you somehow access this site over plain http "
          [:strong "do not use it and please "
           (link-to "mailto:ryan@ryanmcg.com" "let me know immediately!")]]
         [:p "This site is hosted on heroku and uses their piggy-back SSL
             feature.  It also uses mongodb via the MongoHQ heroku addon. "]
         [:p "To ensure the safety of your password it is hashed with bcrypt
             before being stored in mongo."]]]
       [:h3#contact.anchor "Contact"]
       [:p "Hi, my name is Ryan. I made this site and Cadence.js.  There are a
           variety of to contact me. If it's saying \"Hi\" please hit me up "
        (link-to "https://twitter.com/Ryan_VM" "on twitter")"."]
       [:p "If it's some issue with "
        (link-to "https://github.com/RyanMcG/Cadence/issues" "this site") " or "
        (link-to "https://github.com/RyanMcG/Cadence-js/issues" "Cadence.js")
        " use the issues tool on github."]
       [:p "Finally, if it's something else my email is "
        (link-to "mailto:ryan@ryanmcg.com" "ryan@ryanmcg.com") "."]
       ]]]))
