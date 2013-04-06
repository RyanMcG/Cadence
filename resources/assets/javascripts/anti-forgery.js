jQuery(function ($) {
  var ANTI_FORGERY_HEADER = $('meta[name=csrf_header]').attr('content');
  var ANTI_FORGERY_TOKEN = $('meta[name=csrf_token]').attr('content');
  $(document).ajaxSend(function (event, jqXHR, options) {
    // Add anti-forgery header to all AJAX requests
    jqXHR.setRequestHeader(ANTI_FORGERY_HEADER, ANTI_FORGERY_TOKEN);
  });
});
