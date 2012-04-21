/*
 * Cadence.js
 *
 * A typing style monitro for input fields
 *
 * Author: Ryan McGowan
 */

(function (context) {
  (function ($) {
    var Cadence = function (element, submitEl, finishedCallback, options) {
      var resetData = {string: '', timeline: []};
      var cadence = {data: resetData};

      cadence.reset = function () {
        this.data = resetData;
      };
      cadence.cleanUp = function () {
        this.data.string = this.data.timeline[
          this.data.timeline.length - 1].value;
        this.data.timeline.sort(function (a, b) {
          return a.time - b.time;
        });
        for (var i = 0, length = this.data.timeline.length; i < length; i++) {
          eve = this.data.timeline[i];
          if (i === 0) {
            eve.timeDifference = eve.time;
          } else {
            var last_eve = this.data.timeline[i - 1];
            eve.timeDifference = eve.time - last_eve.time;
          }
        }
      };

      var jel = $(element);
      var jsel = $(submitEl);
      var first = true;

      var timeStart;
      cadence.callbackGenerator = function (data, callback) {
        var cb;
        if (callback) {
          cb = callback;
        } else {
          cb = function (event) {
            if (first) {
              first = false;
              timeStart = event.timeStamp;
              lastTime = event.timeStamp;
            }
            data.timeline.push({
              which: event.which,
              type: event.type,
              value: event.target.value,
              time: (event.timeStamp - timeStart)
              //_event: event
            });
          };
        }
        return cb;
      };

      // Set up event listeners
      jel.keydown(cadence.callbackGenerator(cadence.data));
      jel.keyup(cadence.callbackGenerator(cadence.data));

      jsel.submit(function (event) {
        event.preventDefault();
        cadence.cleanUp();
        finishedCallback(cadence);
        cadence.reset();
      });

      return cadence;
    };

      context.Cadence = Cadence;
  })(jQuery);
})(this);
