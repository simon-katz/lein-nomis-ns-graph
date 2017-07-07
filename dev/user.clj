(ns user
  "Namespace to support hacking at the REPL."
  (:require [clojure.java.javadoc :refer [javadoc]]
            [clojure.pprint :refer [pp pprint]]
            [clojure.repl :refer :all]
            [clojure.string :as str]
            [clojure.tools.namespace.move :refer :all]
            [clojure.tools.namespace.repl :refer :all]
            [midje.repl :refer :all]))

;;;; ___________________________________________________________________________
;;;; ---- u-classpath ----

(defn u-classpath []
  (str/split (System/getProperty "java.class.path")
             #":"))

;;;; ___________________________________________________________________________
;;;; ---- u-move-ns-dev-src-test ----

(defn u-move-ns-dev-src-test [old-sym new-sym source-path]
  (move-ns old-sym new-sym source-path ["dev" "src" "test"]))

;;;; ___________________________________________________________________________
;;;; App-specific additional utilities for the REPL or command line
