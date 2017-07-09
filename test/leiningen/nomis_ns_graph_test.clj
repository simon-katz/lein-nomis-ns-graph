(ns leiningen.nomis-ns-graph-test
  (:require [leiningen.nomis-ns-graph :as subject :refer :all]
            [midje.sweet :refer :all]))

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
