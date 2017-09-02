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
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :exclusions-re nil
     :write-gv-file? false})

  (fact "filename"
    (ltu/with-ignore-logging []
      (make-command-line-options [":filename" "the-filename"]))
    =>
    {:filename "the-filename"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :exclusions-re nil
     :write-gv-file? false})

  (fact "platform :clj"
    (ltu/with-ignore-logging []
      (make-command-line-options [":platform" "clj"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :exclusions-re nil
     :write-gv-file? false})

  (fact "source paths"
    (ltu/with-ignore-logging []
      (make-command-line-options [":source-paths" "a/b c/d|e/f"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths ["a/b" "c/d" "e/f"]
     :show-non-project-deps false
     :exclusions nil
     :exclusions-re nil
     :write-gv-file? false})

  (fact "platform :cljs"
    (ltu/with-ignore-logging []
      (make-command-line-options [":platform" "cljs"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :cljs
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :exclusions-re nil
     :write-gv-file? false})

  (fact "show-non-project-deps"
    (ltu/with-ignore-logging []
      (make-command-line-options [":show-non-project-deps"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps true
     :exclusions nil
     :exclusions-re nil
     :write-gv-file? false})

  (fact "show-non-project-deps yeah-man"
    (ltu/with-ignore-logging []
      (make-command-line-options [":show-non-project-deps" "yeah-man"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps true
     :exclusions nil
     :exclusions-re nil
     :write-gv-file? false})

  (fact "show-non-project-deps false"
    (ltu/with-ignore-logging []
      (make-command-line-options [":show-non-project-deps" "false"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :exclusions-re nil
     :write-gv-file? false})

  (fact "exclusions a"
    (ltu/with-ignore-logging []
      (make-command-line-options [":exclusions" "a"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions ["a"]
     :exclusions-re nil
     :write-gv-file? false})

  (fact "exclusions a b"
    (ltu/with-ignore-logging []
      (make-command-line-options [":exclusions" "a b"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions ["a" "b"]
     :exclusions-re nil
     :write-gv-file? false})

  (fact "exclusions a|b"
    (ltu/with-ignore-logging []
      (make-command-line-options [":exclusions" "a|b"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions ["a" "b"]
     :exclusions-re nil
     :write-gv-file? false})

  (fact "exclusions-re a|b"
    (ltu/with-ignore-logging []
      (make-command-line-options [":exclusions-re" "a|b"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :exclusions-re "a|b"
     :write-gv-file? false})

  (fact "write-gv-file?"
    (ltu/with-ignore-logging []
      (make-command-line-options [":write-gv-file?"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :exclusions-re nil
     :write-gv-file? true}))
