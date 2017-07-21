(ns leiningen.nomis-ns-graph.utils.utils)

;;;; ___________________________________________________________________________
;;;; ---- invert-function invert-relation ----

(defn invert-function [f domain-subset]
  "Return a map that represents the inverse of `f`.
  `f` takes elements of `domain-subset` (and possibly other values, not
  relevant here) as argument, and returns a single value.
  For explanations of terminology, see:
    https://www.mathsisfun.com/sets/domain-range-codomain.html
    https://www.mathsisfun.com/sets/injective-surjective-bijective.html"
  (dissoc (group-by f domain-subset)
          nil))

(defn invert-relation [rel domain-subset]
  "Return a map which represents the inverse of `rel`.
  `rel` takes elements of `domain-subset` (and possibly other values, not
  relevant here) as argument, and returns a collection of values.
  For explanations of terminology, see:
    https://www.mathsisfun.com/sets/domain-range-codomain.html
    https://www.mathsisfun.com/sets/injective-surjective-bijective.html"
  (let [domain-range-pairs (for [d domain-subset
                                 r (rel d)]
                             [d r])]
    (reduce (fn [sofar [d r]]
              (update sofar
                      r
                      (fnil conj [])
                      d))
            {}
            domain-range-pairs)))
