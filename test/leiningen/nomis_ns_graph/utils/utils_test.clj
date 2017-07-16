(ns leiningen.nomis-ns-graph.utils.utils-test
  (:require [leiningen.nomis-ns-graph.utils.utils :refer :all]
            [midje.sweet :refer :all]))

;;;; ___________________________________________________________________________
;;;; ---- invert-injection invert-non-injection ----

(fact "`invert-injection` works"
  (invert-injection {:a 1
                     :b 2
                     :c 3
                     :d 1}
                    [:a :b :c :d :e])
  =>
  {1 [:a :d]
   2 [:b]
   3 [:c]})

(fact "`invert-non-injection` works"
  (invert-non-injection {:a [1 2]
                         :b [2 3]
                         :c []
                         :d [2]}
                        [:a :b :c :d :e])
  =>
  {1 [:a]
   2 [:a :b :d]
   3 [:b]})
