// # cadence.js (Source)
//
// A typing style monitor for input fields
//
// Author: Ryan McGowan

(function ($) {
    $.fn.cadence = function (finishedCallback, userOptions) {

      // First we define some helper functions.
      var toKeyCodes = function (instr) {
        var str = instr.toUpperCase();
        var codes = [];
        for (var i = 0, len = str.length; i < len; i++) {
          codes.push(str.charCodeAt(i));
        }
        return codes;
      };

      // Create default options
      var options = {
        userPhraseSel: ".phrase",
        givenPhraseSel: "#given-phrase",
        givenPhrase: null,
        debug: false,
        alertCallback: function (msg, debug) {
          if (debug) {
            console.log("CADENCE ERROR: " + msg);
          }
        }
      };
      // And merge them with what the user gives us.
      $.extend(options, userOptions);

      // Some local variables

      // The cadence object
      var cadence = {timeline: []};

      // Define what the "given phrase" is (i.e. what the user should type in.
      var givenPhrase = options.givenPhrase;
      if (givenPhrase === null) {
        givenPhrase = $(options.givenPhraseSel).text();
      }
      // Turn the given phrase into an array of key codes
      var givenPhraseCodes = toKeyCodes(givenPhrase);

      // Specify where we are in the cadence
      var position = 0;
      // Computer the final position ahead of time.
      var endPosition = givenPhrase.length - 1;

      // The input field the user is typing the cadence into
      var phraseEl = this.find(options.userPhraseSel);

      // Set autocomplete to off since the user must type in the whole phrase
      // anyways.
      phraseEl.attr("autocomplete", "off");

      // Immediately before calling the user supplied callback function we need
      // to clean up our data. This takes the vector of array events held in
      // cadence.timeline and creates a more usable vector of event data in
      // cadence.result.
      cadence.cleanUp = function () {
        this.result = {timeline: []};
        this.timeline.sort(function (a, b) {
          return a.timeStamp - b.timeStamp;
        });
        var startTime;
        var lastTime;
        var completeStr = "";
        for (var i = 0, length = this.timeline.length; i < length; i++) {
          eve = this.timeline[i];
          if (i === 0) {
            startTime = eve.timeStamp;
            lastTime = eve.timeStamp;
          }
          completeStr += String.fromCharCode(eve.keyCode).toLowerCase();
          this.result.timeline.push({
              time: eve.timeStamp - startTime,
              timeDifference: eve.timeStamp - lastTime,
              keyCode: eve.keyCode,
              character: String.fromCharCode(eve.keyCode).toLowerCase()
          });
          lastTime = eve.timeStamp;
        }
        this.result.phrase = completeStr;
      };

      // The reset function is called whenever the user input field needs to be
      // reset. This occurs either on completion of entering a phrase or when
      // the user enters anything incorrectly.
      cadence.reset = function (alert) {
        position = 0;
        if (typeof alert !== 'undefined' && alert) {
          options.alertCallback("You are no longer typing in the " +
              "given phrase ('" + givenPhrase +"').", options.debug);
        }
        cadence.timeline = [];
        phraseEl.val("");
      };

      // A callback function for when there is a keyup event in the user input
      // field.
      cadence.logKeyUp = function (event) {
        if (givenPhraseCodes[position] === event.keyCode) {
          cadence.timeline.push(event);
          if (position === endPosition) {
            cadence.cleanUp();
            finishedCallback($.extend({}, cadence.result));
            cadence.reset();
          } else {
            position++;
          }
        } else {
          cadence.reset(true);
        }
      };

      // Set up event listeners
      phraseEl.keyup(cadence.logKeyUp);

      // Prevent the default form submission.
      this.submit(function (event) {
        event.preventDefault();
      });

      // That's all folks!
      return this;
    };
})(jQuery);
