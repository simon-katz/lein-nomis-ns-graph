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
    {:filename "nomis-ns-graph-clj"
     :platform :clj
     :show-non-project-deps nil
     :exclusions []})

  (fact "filename"
    (ltu/with-ignore-logging []
     (#'subject/make-options [":filename" "the-filename"]))
    =>
    {:filename "the-filename"
     :platform :clj
     :show-non-project-deps nil
     :exclusions []})

  (fact "platform :clj"
    (ltu/with-ignore-logging []
     (#'subject/make-options [":platform" "clj"]))
    =>
    {:filename "nomis-ns-graph-clj"
     :platform :clj
     :show-non-project-deps nil
     :exclusions []})

  (fact "platform :cljs"
    (ltu/with-ignore-logging []
     (#'subject/make-options [":platform" "cljs"]))
    =>
    {:filename "nomis-ns-graph-cljs"
     :platform :cljs
     :show-non-project-deps nil
     :exclusions []})

  (fact "show-non-project-deps"
    (ltu/with-ignore-logging []
     (#'subject/make-options [":show-non-project-deps"]))
    =>
    {:filename "nomis-ns-graph-clj-with-externals"
     :platform :clj
     :show-non-project-deps true
     :exclusions []})

  (fact "show-non-project-deps yeah-man"
    (ltu/with-ignore-logging []
     (#'subject/make-options [":show-non-project-deps" "yeah-man"]))
    =>
    {:filename "nomis-ns-graph-clj-with-externals"
     :platform :clj
     :show-non-project-deps "yeah-man" ; FIXME
     :exclusions []})

  (fact "show-non-project-deps false"
    (ltu/with-ignore-logging []
     (#'subject/make-options [":show-non-project-deps" "false"]))
    =>
    {:filename "nomis-ns-graph-clj-with-externals"
     :platform :clj
     :show-non-project-deps "false" ; FIXME
     :exclusions []})

  (fact "exclusions a"
    (ltu/with-ignore-logging []
     (#'subject/make-options [":exclusions" "a"]))
    =>
    {:filename "nomis-ns-graph-clj"
     :platform :clj
     :show-non-project-deps nil
     :exclusions ["a"]})

  (fact "exclusions a b"
    (ltu/with-ignore-logging []
     (#'subject/make-options [":exclusions" "a b"]))
    =>
    {:filename "nomis-ns-graph-clj"
     :platform :clj
     :show-non-project-deps nil
     :exclusions ["a" "b"]})

  (fact "exclusions a|b"
    (ltu/with-ignore-logging []
     (#'subject/make-options [":exclusions" "a|b"]))
    =>
    {:filename "nomis-ns-graph-clj"
     :platform :clj
     :show-non-project-deps nil
     :exclusions ["a" "b"]}))

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
