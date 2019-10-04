(ns leiningen.lein-test-utils
  (:require [leiningen.core.main :as lcm]
            [midje.sweet :refer :all]))

;;;; ___________________________________________________________________________
;;;; ---- with-ignore-logging ----

(defn -with-ignore-logging* [fun]
  ;; This has to be public, otherwise uses of `with-ignore-logging` fail.
  (binding [lcm/*info* false]
    (fun)))

(defmacro with-ignore-logging [[] & body]
  `(-with-ignore-logging* (fn [] ~@body)))

;;;; - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

(fact "`with-ignore-logging` works"
  (with-out-str
    (with-ignore-logging []
      (lcm/info "Hi")
      (print "Bye")))
  => "Bye")
