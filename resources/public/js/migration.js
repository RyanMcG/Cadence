(function ($) {
  var _SEMAPOHRES = {};

  var Semaphore = function (key) {
    this.key = key;
    if (_SEMAPOHRES[this.key] == undefined)
      _SEMAPOHRES[this.key] = false;

    return this;
  };

  Semaphore.prototype.isLocked = function() {
    return _SEMAPOHRES[this.key];
  };

  Semaphore.prototype.lock = function() {
    _SEMAPOHRES[this.key] = true;
    return this;
  };

  Semaphore.prototype.unlock = function() {
    _SEMAPOHRES[this.key] = false;
    return this;
  };

  Semaphore.wrap = function (key, callback) {
    var semaphore = new this(key);
    if (!semaphore.isLocked()) {
      semaphore.lock()
      callback(semaphore);
    }
  };

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
      onClass: 'btn-primary',
      offClass: 'btn-inverse',
      removeSuccessFromLabel: false,
      labelText: '<i class="icon-white icon-ok-sign"></i> Applied'
    },
    Apply: {
      text: 'Rollback',
      onClass: 'btn-inverse',
      offClass: 'btn-primary',
      removeSuccessFromLabel: true,
      labelText: '<i class="icon-white icon-remove-sign"></i> Not Applied'
    }
  };
  var successLabelClass = 'label-success';
  var toggleButtonState = function ($button) {
    var values = actionState[$button.text()];
    $button.text(values.text);
    $button.removeClass(values.offClass);
    $button.addClass(values.onClass);
  };

  var toggleLabelState = function (action, $label) {
    var state = actionState[action];
    if (state.removeSuccessFromLabel) {
      $label.removeClass(successLabelClass);
    } else {
      $label.addClass(successLabelClass);
    }
    $label.html(state.labelText);
  };

  $('table#migrations').on('click', '.controls button', function () {
    var $this = $(this);
    var objId = $this.data('objectId');
    var action = $this.text();
    var $label = $('#migration-' + objId + ' td.applied span.label');

    // Toggle the button immediately
    toggleButtonState($this);

    Semaphore.wrap(objId, function (semaphore) {
      $.post("/admin/migrations", {
        object_id: objId,
        action: action
      }).done(function () {
        // On success change the label color.
        toggleLabelState($this.text(), $label);
      }).fail(function () {
        // If we failed toggle the button back.
        toggleButtonState($this);
      }).always(function () {
        semaphore.unlock();
      });
    });
  });
})(jQuery);
