jQuery(function ($) {
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

  $('#migrations').on('click', '.controls button', function () {
    var $this = $(this);
    var objId = $this.data('objectId');
    var action = $this.text();
    var $label = $('#migration-' + objId + ' .applied span.label');

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
});
