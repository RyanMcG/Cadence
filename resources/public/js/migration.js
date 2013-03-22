(function ($) {

  var ANTI_FORGERY_HEADER = $('meta[name=csrf_header]').attr('content');
  var ANTI_FORGERY_TOKEN = $('meta[name=csrf_token]').attr('content');
  $(document).ajaxSend(function (event, jqXHR, options) {
    // Add anti-forgery header to all AJAX requests
    jqXHR.setRequestHeader(ANTI_FORGERY_HEADER, ANTI_FORGERY_TOKEN);

    // If the data is of type json, jsonify it.
    if (options.data != null && options.dataType == 'json')
      options.data = JSON.stringify(options.data);
  });

  var actionState = {
    Rollback: {
      text: 'Apply',
      onClass: 'btn-info',
      offClass: 'btn-inverse'
    },
    Apply: {
      text: 'Rollback',
      onClass: 'btn-inverse',
      offClass: 'btn-info'
    }
  };

  $('table#migrations').on('click', '.controls button', function () {
    var $this = $(this);
    var objId = $this.data('objectId');
    var action = $this.text();

    var values = actionState[action];
    var toggleButtonState = function ($button) {
      $button.text(values.text);
      $button.removeClass(values.offClass);
      $button.addClass(values.onClass);
    };

    $.post("/admin/migrations", {
      object_id: objId,
      action: action
    }).done(function () {
      toggleButtonState($this);
    }).fail(function () {
      console.log("Failed");
    });
  });
})(jQuery);
