(ns leiningen.nomis-ns-graph.p200-graphing.graph-test
  (:require [clojure.java.io :as io]
            [leiningen.lein-test-utils :as ltu]
            [leiningen.nomis-ns-graph.p200-graphing.graph :as subject :refer :all]
            [midje.sweet :refer :all]))

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

;;;; ___________________________________________________________________________

(defn check-graphing [filename
                      ns-graph-spec]
  (let [dir "test-resources/output/ns-graphing/"
        expected-file-name-base (str dir filename)
        [expected-file-name
         actual-file-name] [(str expected-file-name-base "-expected.gv")
                            (str expected-file-name-base "-actual.gv")]]
    (let [dot-data (ns-graph-spec->dot-data ns-graph-spec)]
      (when
          ;; Take care to only do this when necessary, so that diff tools
          ;; don't show unnecessary diffs.
          (or (not (.exists (io/file actual-file-name)))
              (not= dot-data
                    (slurp actual-file-name)))
        (spit actual-file-name
              dot-data))
      (fact (slurp expected-file-name)
        => dot-data))))

(fact "clj"
  (check-graphing
   "nomis-ns-graph-clj"
   {:platform              :clj
    :source-paths          ["test-resources/example-projects/nomisdraw/dev"
                            "test-resources/example-projects/nomisdraw/src/clj"]
    :exclusions            ["user" "midje"]
    :show-non-project-deps false
    :project-group         "nomisdraw"
    :project-name          "nomisdraw"}))

(fact "clj-with-externals"
  (check-graphing
   "nomis-ns-graph-clj-with-externals"
   {:platform              :clj
    :source-paths          ["test-resources/example-projects/nomisdraw/dev"
                            "test-resources/example-projects/nomisdraw/src/clj"]
    :exclusions            ["user" "midje"]
    :show-non-project-deps true
    :project-group         "nomisdraw"
    :project-name          "nomisdraw"}))

(fact "cljs"
  (check-graphing
   "nomis-ns-graph-cljs"
   {:platform              :cljs
    :source-paths          ["test-resources/example-projects/nomisdraw/src/cljs"
                            "test-resources/example-projects/nomisdraw/cljs/src"]
    :exclusions            nil
    :show-non-project-deps false
    :project-group         "nomisdraw"
    :project-name          "nomisdraw"}))

(fact "cljs-with-externals"
  (check-graphing
   "nomis-ns-graph-cljs-with-externals"
   {:platform              :cljs
    :source-paths          ["test-resources/example-projects/nomisdraw/src/cljs"
                            "test-resources/example-projects/nomisdraw/cljs/src"]
    :exclusions            nil
    :show-non-project-deps true
    :project-group         "nomisdraw"
    :project-name          "nomisdraw"}))
