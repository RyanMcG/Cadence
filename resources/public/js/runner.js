// # Cadence server application usage.
(function (context) {
  jQuery(document).ready(function ($) {
    // Set the contentType to application/json
    $.ajaxSetup({
        contentType: 'application/json',
        processData: false
    });
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
      $.post("/user/training", JSON.stringify(result), function (data, textStatus, jqXHR) {
        // Always clear alerts whenever we get new feedback from the server.
        $feedback.clearAlerts();

        console.log(data);
        if (!data.success) {
          $feedback.append(
            generateAlert("alert-error",
              "It appears that the supplied cadence result was invalid.")
          );
        } else if (data.progress === 0) {
          $feedback.append(
            generateAlert("alert-warning",
              "<p>That cadence you entered didn't really help with training.</p>" +
              "<p>Did you type in the given phrase differently from before?</p>")
          );
        }
        if (data.done) {
          $feedback.append(
            generateAlert("alert-success",
              "<p>Yay! You've done enough training. Feel free to continue " +
                "training, but be aware:</p>" +
                "<p><em>Your training data will <strong>not</strong> be " +
                "stored until you visit your profile page.<em></p>")
          );

          var $comp = $("#completion");
          $comp.find("button").text("I'm done!").removeClass("disabled").addClass("btn-success");
          $comp.find(".progress").addClass("active");
        } else {
          // Modify the size of the progress bar.
          progressBar.css("width", (data.progress + "%"));
        }
      }, 'json')
      .error(function () {
        $feedback.clearAlerts();
        $feedback.append(
          generateAlert("alert-error",
            "<strong>Uh oh!</strong> It appears an error occurred on my side.")
        );
      });
    });
  });
})(this);
