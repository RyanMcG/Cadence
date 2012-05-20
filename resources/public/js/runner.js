// # Cadence server application usage.
(function (context) {
  jQuery(document).ready(function ($) {
    function postForm (selector) {
      var $form = $(selector);
      $form.find("button").submit(function (e) {
        e.preventDefault();
        var formData = $form.serialize();
        $.post($form.attr("action"), formData, function (data) {
          if (!data.success) {
            alert("Unsuccessful.");
          }
        });
      });
    }

    $("form#trainer").cadence(function (result) {
      console.log(result);
    });
  });
})(this);
