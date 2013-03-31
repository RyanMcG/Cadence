// # Cadence server application usage.
jQuery(function ($) {
  var progressBar = $("div#completion div.progress div.bar");
  var $feedback = $("div#feedback");
  $feedback.clearAlerts = function () {
    this.find(".alert").remove();
  };

  var generateAlert = function (cls, message) {
    var alert = '<div class="alert fade in ' + cls + '">';
    alert += '<a class="close" data-dismiss="alert">&times;</a>';
    alert += message;
    alert += '</div>';
    return alert;
  };

  $("form#trainer").cadence(function (result) {
    $.post("/user/training", result, function (data, textStatus, jqXHR) {
      // Always clear alerts whenever we get new feedback from the server.
      $feedback.clearAlerts();

      if (!data.success) {
        $feedback.append( generateAlert("alert-error",
                        "It appears that the supplied cadence result was invalid."));
      } else if (data.progress === 0) {
        $feedback.append(generateAlert("alert-warning",
                        "<p>That cadence you entered didn't really help with training.</p>" +
                          "<p>Did you type in the given phrase differently from before?</p>"));
      }
      if (data.done) {
        $feedback.append(
          generateAlert("alert-success",
                        "<p>Yay! You've done enough training. Feel free to continue " +
                          "training, but be aware:</p>" +
                          "<h4>Your training data will <strong>not</strong> be " +
                          "stored until you <a href=\"/user/profile\">" +
                          "visit your profile</a>.</h4></p>"));
        var $comp = $("#completion");
        var $cButton = $comp.find("a.btn");
        $cButton.text("Click me to complete your training!").removeClass("disabled").addClass("btn-success");
        var $pBar = $comp.find(".progress").addClass("active");
        $pBar.removeClass("span10").addClass("span8");
        $cButton.parent().removeClass("span2").addClass("span4");
        $cButton.attr("href", "/user/profile");
      }
      // Modify the size of the progress bar.
      var progressBarWidth = data.progress;
      if (progressBarWidth > 100) {
        progressBarWidth = 100;
      } else if (progressBarWidth < 0) {
        progressBarWidth = 0;
      }
      progressBar.css("width", (progressBarWidth + "%"));
    }, 'json')
    .fail(function () {
      $feedback.clearAlerts();
      $feedback.append(
        generateAlert("alert-error",
                      "<strong>Uh oh!</strong> It appears an error occurred on my side.")
      );
    });
  });

  // Authenticate
  $("form#authenticate").cadence(function (result) {
    $.post("/user/auth", result, function (data, textStatus, jqXHR) {
      // Always clear alerts whenever we get new feedback from the server.
      $feedback.clearAlerts();
      if (data.success) {
        $feedback.append(generateAlert("alert-" + data.type, data.message));
        $feedback.append(
          generateAlert("alert-info",
                        "<h4>Last Result:</h4>" +
                          "<pre>" + JSON.stringify(data, null, "   ") + "</pre>")
        );
      } else {
        $.each(data.errors, function(index, value) {
          $feedback.append(generateAlert("alert-error", value));
        });
      }
    }, 'json')
    .fail(function () {
      $feedback.clearAlerts();
      $feedback.append(
        generateAlert("alert-error",
                      "<strong>Uh oh!</strong> It appears an error occurred on my side.")
      );
    });
  });
});
