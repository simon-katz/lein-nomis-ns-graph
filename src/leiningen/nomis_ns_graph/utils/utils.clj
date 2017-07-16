(ns leiningen.nomis-ns-graph.utils.utils)

;;;; ___________________________________________________________________________
;;;; ---- invert-injection invert-non-injection ----

(defn invert-injection [f domain]
  "Return a map which, when regarded as a function, is the inverse of `f`.
  `f` is an injective function -- it takes elements of `domain` (and possibly
  other values, not relevant here) as argument, and returns a single value.
  For explanations of terminology, see:
    https://www.mathsisfun.com/sets/domain-range-codomain.html
    https://www.mathsisfun.com/sets/injective-surjective-bijective.html"
  (dissoc (group-by f domain)
          nil))

(defn invert-non-injection [f domain]
  "Return a map which, when regarded as a function, is the inverse of `f`.
  `f` is an non-injective function -- it takes elements of `domain` (and
  possibly other values, not relevant here) as argument, and returns acollection
  of values.
  For explanations of terminology, see:
    https://www.mathsisfun.com/sets/domain-range-codomain.html
    https://www.mathsisfun.com/sets/injective-surjective-bijective.html"
  (let [range-domain-pairs (for [d domain
                                 r (f d)]
                             [d r])]
    (reduce (fn [sofar [d r]]
              (update sofar
                      r
                      (fnil conj [])
                      d))
            {}
            range-domain-pairs)))
