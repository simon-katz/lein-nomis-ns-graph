(ns leiningen.nomis-ns-graph.p100-args-and-options.p100-args-test
  (:require [leiningen.lein-test-utils :as ltu]
            [leiningen.nomis-ns-graph.p100-args-and-options.p100-args :refer :all]
            [midje.sweet :refer :all]))

;;;; ___________________________________________________________________________

(fact `make-command-line-options` works

  (fact "No args"
    (ltu/with-ignore-logging []
      (make-command-line-options []))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj})

  (fact "filename"
    (ltu/with-ignore-logging []
      (make-command-line-options [":filename" "the-filename"]))
    =>
    {:filename "the-filename"
     :platform :clj})

  (fact "platform :clj"
    (ltu/with-ignore-logging []
      (make-command-line-options [":platform" "clj"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj})

  (fact "source paths"
    (ltu/with-ignore-logging []
      (make-command-line-options [":source-paths" "a/b c/d|e/f"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths ["a/b" "c/d" "e/f"]})

  (fact "platform :cljs"
    (ltu/with-ignore-logging []
      (make-command-line-options [":platform" "cljs"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :cljs})

  (fact "show-non-project-deps"
    (ltu/with-ignore-logging []
      (make-command-line-options [":show-non-project-deps"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :show-non-project-deps true})

  (fact "show-non-project-deps yeah-man"
    (ltu/with-ignore-logging []
      (make-command-line-options [":show-non-project-deps" "yeah-man"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :show-non-project-deps true})

  (fact "show-non-project-deps false"
    (ltu/with-ignore-logging []
      (make-command-line-options [":show-non-project-deps" "false"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj})

  (fact "inclusions a"
    (ltu/with-ignore-logging []
      (make-command-line-options [":inclusions" "a"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :inclusions ["a"]})

  (fact "inclusions a b"
    (ltu/with-ignore-logging []
      (make-command-line-options [":inclusions" "a b"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :inclusions ["a" "b"]})

  (fact "inclusions a|b"
    (ltu/with-ignore-logging []
      (make-command-line-options [":inclusions" "a|b"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :inclusions ["a" "b"]})

  (fact "inclusions-re a|b"
    (ltu/with-ignore-logging []
      (make-command-line-options [":inclusions-re" "a|b"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :inclusions-re "a|b"})

  (fact "exclusions a"
    (ltu/with-ignore-logging []
      (make-command-line-options [":exclusions" "a"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :exclusions ["a"]})

  (fact "exclusions a b"
    (ltu/with-ignore-logging []
      (make-command-line-options [":exclusions" "a b"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :exclusions ["a" "b"]})

  (fact "exclusions a|b"
    (ltu/with-ignore-logging []
      (make-command-line-options [":exclusions" "a|b"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :exclusions ["a" "b"]})

  (fact "exclusions-re a|b"
    (ltu/with-ignore-logging []
      (make-command-line-options [":exclusions-re" "a|b"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :exclusions-re "a|b"})

  (fact "write-gv-file?"
    (ltu/with-ignore-logging []
      (make-command-line-options [":write-gv-file?"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :write-gv-file? true}))
