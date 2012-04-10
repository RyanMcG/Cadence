(ns cadence.repl
  (:use [cljs.repl.browser :only (repl-env)])
  (:require [cljs.repl]))

(defn repljs []
  (cljs.repl/repl (repl-env)))
