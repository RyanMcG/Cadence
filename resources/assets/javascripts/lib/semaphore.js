(function (context) {
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

  var conflictingSemaphore = context.Semaphore;

  Semaphore.noConflict = function () {
    context.Semaphore = conflictingSemaphore;
    return Semaphore;
  };

  context.Semaphore = Semaphore;
})(this);
