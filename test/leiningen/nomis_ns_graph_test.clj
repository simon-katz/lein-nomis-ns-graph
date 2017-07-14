(ns leiningen.nomis-ns-graph-test
  (:require [leiningen.lein-test-utils :as ltu]
            [leiningen.nomis-ns-graph :as subject :refer :all]
            [midje.sweet :refer :all]))

;;;; ___________________________________________________________________________

(fact `make-options` works

  (fact "No args"
    (ltu/with-ignore-logging []
      (#'subject/make-options []))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :write-gv-file? false})

  (fact "filename"
    (ltu/with-ignore-logging []
      (#'subject/make-options [":filename" "the-filename"]))
    =>
    {:filename "the-filename"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :write-gv-file? false})

  (fact "platform :clj"
    (ltu/with-ignore-logging []
      (#'subject/make-options [":platform" "clj"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :write-gv-file? false})

  (fact "source paths"
    (ltu/with-ignore-logging []
      (#'subject/make-options [":source-paths" "a/b c/d|e/f"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths ["a/b" "c/d" "e/f"]
     :show-non-project-deps false
     :exclusions nil
     :write-gv-file? false})

  (fact "platform :cljs"
    (ltu/with-ignore-logging []
      (#'subject/make-options [":platform" "cljs"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :cljs
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :write-gv-file? false})

  (fact "show-non-project-deps"
    (ltu/with-ignore-logging []
      (#'subject/make-options [":show-non-project-deps"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps true
     :exclusions nil
     :write-gv-file? false})

  (fact "show-non-project-deps yeah-man"
    (ltu/with-ignore-logging []
      (#'subject/make-options [":show-non-project-deps" "yeah-man"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps true
     :exclusions nil
     :write-gv-file? false})

  (fact "show-non-project-deps false"
    (ltu/with-ignore-logging []
      (#'subject/make-options [":show-non-project-deps" "false"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :write-gv-file? false})

  (fact "exclusions a"
    (ltu/with-ignore-logging []
      (#'subject/make-options [":exclusions" "a"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions ["a"]
     :write-gv-file? false})

  (fact "exclusions a b"
    (ltu/with-ignore-logging []
      (#'subject/make-options [":exclusions" "a b"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions ["a" "b"]
     :write-gv-file? false})

  (fact "exclusions a|b"
    (ltu/with-ignore-logging []
      (#'subject/make-options [":exclusions" "a|b"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions ["a" "b"]
     :write-gv-file? false})

  (fact "write-gv-file?"
    (ltu/with-ignore-logging []
      (#'subject/make-options [":write-gv-file?"]))
    =>
    {:filename "nomis-ns-graph"
     :platform :clj
     :source-paths nil
     :show-non-project-deps false
     :exclusions nil
     :write-gv-file? true}))

;;;; ___________________________________________________________________________

(fact "`ns-symbol->pieces` works"
  (#'subject/ns-symbol->pieces "a.bb.ccc")
  => '[a bb ccc])

(fact "`ns-symbol->last-piece` works"
  (#'subject/ns-symbol->last-piece "a.bb.ccc")
  => 'ccc)


(fact "`ns-symbol->parent-ns-symbol` works"
  
  (fact "without parent"
    (#'subject/ns-symbol->parent-ns-symbol "a")
    => nil)
  
  (fact "with parent"
    (#'subject/ns-symbol->parent-ns-symbol "a.bb.ccc")
    => 'a.bb))


(fact "`ns-symbol->all-parent-ns-symbols-incl-self` works"

  (fact
    (#'subject/ns-symbol->all-parent-ns-symbols-incl-self 'a.bb.ccc)
    => '[a.bb.ccc a.bb a])

  (fact
    (#'subject/ns-symbol->all-parent-ns-symbols-incl-self 'a)
    => '[a]))
