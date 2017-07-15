(ns leiningen.nomis-ns-graph.args-test
  (:require [leiningen.lein-test-utils :as ltu]
            [leiningen.nomis-ns-graph.args :refer :all]
            [midje.sweet :refer :all]))

;;;; ___________________________________________________________________________

(fact `make-options` works

  (fact "No args"
    (ltu/with-ignore-logging []
      (make-options []))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :write-gv-file? false})

  (fact "filename"
    (ltu/with-ignore-logging []
      (make-options [":filename" "the-filename"]))
    =>
    {:filename "the-filename"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :write-gv-file? false})

  (fact "platform :clj"
    (ltu/with-ignore-logging []
      (make-options [":platform" "clj"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :write-gv-file? false})

  (fact "source paths"
    (ltu/with-ignore-logging []
      (make-options [":source-paths" "a/b c/d|e/f"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths ["a/b" "c/d" "e/f"]
     :show-non-project-deps false
     :exclusions nil
     :write-gv-file? false})

  (fact "platform :cljs"
    (ltu/with-ignore-logging []
      (make-options [":platform" "cljs"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :cljs
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :write-gv-file? false})

  (fact "show-non-project-deps"
    (ltu/with-ignore-logging []
      (make-options [":show-non-project-deps"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps true
     :exclusions nil
     :write-gv-file? false})

  (fact "show-non-project-deps yeah-man"
    (ltu/with-ignore-logging []
      (make-options [":show-non-project-deps" "yeah-man"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps true
     :exclusions nil
     :write-gv-file? false})

  (fact "show-non-project-deps false"
    (ltu/with-ignore-logging []
      (make-options [":show-non-project-deps" "false"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :write-gv-file? false})

  (fact "exclusions a"
    (ltu/with-ignore-logging []
      (make-options [":exclusions" "a"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions ["a"]
     :write-gv-file? false})

  (fact "exclusions a b"
    (ltu/with-ignore-logging []
      (make-options [":exclusions" "a b"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions ["a" "b"]
     :write-gv-file? false})

  (fact "exclusions a|b"
    (ltu/with-ignore-logging []
      (make-options [":exclusions" "a|b"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions ["a" "b"]
     :write-gv-file? false})

  (fact "write-gv-file?"
    (ltu/with-ignore-logging []
      (make-options [":write-gv-file?"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :write-gv-file? true}))
