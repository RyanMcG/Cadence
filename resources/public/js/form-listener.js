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
    postForm("#login");
    postForm("#signup");
  });
})(this);
