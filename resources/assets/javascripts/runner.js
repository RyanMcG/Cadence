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

  var handleErrors = function (jqXHR) {
    if (jqXHR.status >= 400 && jqXHR.status < 500) {
      var data = JSON.parse(jqXHR.responseText);
      $.each(data.errors, function(index, value) {
        $feedback.append(generateAlert("alert-error", value));
      });
    } else {
      $feedback.clearAlerts();
      $feedback.append(
        generateAlert("alert-error",
                      "<strong>Uh oh!</strong> It appears an error occurred on my side.")
      );
    }
  };

  $("form#trainer").cadence(function (result) {
    $.ajax({
      url: '/user/training',
      type: 'POST',
      data: JSON.stringify(result),
      contentType: 'application/json; charset=UTF-8'
    }).done(function (data, textStatus, jqXHR) {
      // Always clear alerts whenever we get new feedback from the server.
      $feedback.clearAlerts();

      if (data.progress >= 100) {
        $feedback.append(
          generateAlert("alert-success",
                        "<p>Yay! You've done enough training. Feel free to " +
                        " continue training, or refresh the page to traing " +
                        " another phrase.</p>"));
        var $comp = $("#completion");
        var $cButton = $comp.find("a.btn");
        $cButton.addClass("btn-success");
        $comp.find(".progress").addClass("active");
      }

      // Modify the size of the progress bar.
      var progressBarWidth = data.progress;
      if (progressBarWidth > 100) {
        progressBarWidth = 100;
      } else if (progressBarWidth < 0) {
        progressBarWidth = 0;
      }
      progressBar.css("width", (progressBarWidth + "%"));
    }).fail(handleErrors);
  });

  // Authenticate
  $("form#authenticate").cadence(function (result) {
    $.ajax({
      url: '/user/auth',
      type: 'POST',
      data: JSON.stringify(result),
      contentType: 'application/json; charset=UTF-8'
    }).done(function (data, textStatus, jqXHR) {
      // Always clear alerts whenever we get new feedback from the server.
      $feedback.clearAlerts();
      $feedback.append(generateAlert("alert-" + data.type, data.message));
      $feedback.append(
        generateAlert("alert-info",
                      "<h4>Last Result:</h4>" +
                        "<pre>" + JSON.stringify(data, null, "   ") + "</pre>")
      );
    }).fail(handleErrors);
  });
});
