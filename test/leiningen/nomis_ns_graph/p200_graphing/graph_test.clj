(ns leiningen.nomis-ns-graph.p200-graphing.graph-test
  (:require [clojure.java.io :as io]
            [leiningen.lein-test-utils :as ltu]
            [leiningen.nomis-ns-graph.p200-graphing.graph :as subject :refer :all]
            [midje.sweet :refer :all]))

;;;; ___________________________________________________________________________

(fact "`nsn->pieces` works"
  (#'subject/nsn->pieces "a.bb.ccc")
  => '[a bb ccc])

(fact "`nsn->last-piece` works"
  (#'subject/nsn->last-piece "a.bb.ccc")
  => 'ccc)


(fact "`nsn->parent-nsn` works"
  
  (fact "without parent"
    (#'subject/nsn->parent-nsn "a")
    => nil)
  
  (fact "with parent"
    (#'subject/nsn->parent-nsn "a.bb.ccc")
    => 'a.bb))


(fact "`nsn->all-parent-nsns-incl-self` works"

  (fact
    (#'subject/nsn->all-parent-nsns-incl-self 'a.bb.ccc)
    => '[a.bb.ccc a.bb a])

  (fact
    (#'subject/nsn->all-parent-nsns-incl-self 'a)
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
          ;; don't show unnecessary diffs in file timestamps.
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
    :project-group         "nomisdraw"
    :project-name          "nomisdraw"}))

(fact "clj-with-inclusions-and-externals"
  (check-graphing
   "nomis-ns-graph-clj-with-inclusions-and-externals"
   {:platform              :clj
    :source-paths          ["test-resources/example-projects/nomisdraw/dev"
                            "test-resources/example-projects/nomisdraw/src/clj"]
    :inclusions            ["user" "midje"]
    :show-non-project-deps true
    :project-group         "nomisdraw"
    :project-name          "nomisdraw"}))

(fact "clj-with-exclusions-and-externals"
  (check-graphing
   "nomis-ns-graph-clj-with-exclusions-and-externals"
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
    :project-group         "nomisdraw"
    :project-name          "nomisdraw"}))

(fact "cljs-with-externals"
  (check-graphing
   "nomis-ns-graph-cljs-with-externals"
   {:platform              :cljs
    :source-paths          ["test-resources/example-projects/nomisdraw/src/cljs"
                            "test-resources/example-projects/nomisdraw/cljs/src"]
    :show-non-project-deps true
    :project-group         "nomisdraw"
    :project-name          "nomisdraw"}))

(fact "clj-with-inclusions-re"
  (check-graphing
   "nomis-ns-graph-clj-with-inclusions-re"
   {:platform              :clj
    :source-paths          ["test-resources/example-projects/nomisdraw/dev"
                            "test-resources/example-projects/nomisdraw/src/clj"]
    :inclusions-re         "u.er|\\.sys"
    :project-group         "nomisdraw"
    :project-name          "nomisdraw"}))

(fact "clj-with-exclusions-re"
  (check-graphing
   "nomis-ns-graph-clj-with-exclusions-re"
   {:platform              :clj
    :source-paths          ["test-resources/example-projects/nomisdraw/dev"
                            "test-resources/example-projects/nomisdraw/src/clj"]
    :exclusions-re         "u.er|\\.sys"
    :project-group         "nomisdraw"
    :project-name          "nomisdraw"}))

(fact "clj-with-inclusions-and-inclusions-re"
  (check-graphing
   "nomis-ns-graph-clj-with-inclusions-and-inclusions-re"
   {:platform              :clj
    :source-paths          ["test-resources/example-projects/nomisdraw/dev"
                            "test-resources/example-projects/nomisdraw/src/clj"]
    :inclusions            ["user" "nomisdraw.services.provided.web.server"]
    :inclusions-re         "u.er|\\.sys"
    :project-group         "nomisdraw"
    :project-name          "nomisdraw"}))

(fact "clj-with-exclusions-and-exclusions-re"
  (check-graphing
   "nomis-ns-graph-clj-with-exclusions-and-exclusions-re"
   {:platform              :clj
    :source-paths          ["test-resources/example-projects/nomisdraw/dev"
                            "test-resources/example-projects/nomisdraw/src/clj"]
    :exclusions            ["user"]
    :exclusions-re         "\\.sys"
    :project-group         "nomisdraw"
    :project-name          "nomisdraw"}))

(fact "clj-with-inclusions-re-and-exclusions-re"
  (check-graphing
   "nomis-ns-graph-clj-with-inclusions-re-and-exclusions-re"
   {:platform              :clj
    :source-paths          ["test-resources/example-projects/nomisdraw/dev"
                            "test-resources/example-projects/nomisdraw/src/clj"]
    :inclusions-re         "\\.services\\."
    :exclusions-re         "\\.handlers\\."
    :project-group         "nomisdraw"
    :project-name          "nomisdraw"}))
