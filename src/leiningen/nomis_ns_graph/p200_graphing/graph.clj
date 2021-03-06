(ns leiningen.nomis-ns-graph.p200-graphing.graph
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.tools.namespace.dependency :as ctns-dep]
            [clojure.tools.namespace.file :as ctns-file]
            [clojure.tools.namespace.find :as ctns-find]
            [clojure.tools.namespace.parse :as ctns-parse]
            [clojure.tools.namespace.track :as ctns-track]
            [leiningen.core.main :as lcm]
            [leiningen.nomis-ns-graph.utils.utils :as u]
            [rhizome.dot :as dot]))

;;;; ___________________________________________________________________________

;;;; "nsn" means "ns-name".

;;;; ___________________________________________________________________________
;;;; Domain stuff

(defn ^:private nsn->pieces [nsn]
  (as-> nsn __
    (name __)
    (str/split __ #"\.")
    (map symbol __)))

(defn ^:private nsn->last-piece [nsn]
  (-> nsn
      nsn->pieces
      last))

(defn ^:private pieces->nsn [pieces]
  (->> pieces
       (str/join ".")
       symbol))

(defn ^:private nsn->parent-nsn [nsn]
  (as-> nsn __
    (name __)
    (str/split __ #"\.")
    (butlast __)
    (str/join "." __)
    (if (= __ "")
      nil
      (symbol __))))

(defn ^:private nsn->all-parent-nsns-incl-self [nsn]
  (as-> nsn __
    (name __)
    (str/split __ #"\.")
    (iterate butlast __)
    (take-while (comp not nil?)
                __)
    (map pieces->nsn __)))

(defn ^:private nsns->all-nsns [nsns]
  (apply set/union
         (map (comp set nsn->all-parent-nsns-incl-self)
              nsns)))

(defn ^:private dups [seq]
  (for [[id freq] (frequencies seq)
        :when (> freq 1)]
    id))

(defn ^:private compute-source-files [platform
                                      source-paths]
  (let [platform-for-ctns (case platform
                            :clj ctns-find/clj
                            :cljs ctns-find/cljs)]
    (apply set/union
           (map (comp #(ctns-find/find-sources-in-dir %
                                                      platform-for-ctns)
                      io/file)
                source-paths))))

(defn ^:private source-files->dep-graph [platform
                                         source-files]
  (let [read-opts (case platform
                    :clj  ctns-parse/clj-read-opts
                    :cljs ctns-parse/cljs-read-opts)]
    (let [tracker (ctns-file/add-files {}
                                       source-files
                                       read-opts)]
      (tracker ::ctns-track/deps))))

(defn ^:private source-files->nsns [source-files]
  (->> (map (comp second ctns-file/read-file-ns-decl)
            source-files)
       (remove nil?)
       set))

(defn  ^:private leaf-nsns->nsn->self-and-children [leaf-nsns]
  (reduce (fn [sofar [n p]]
            (update sofar
                    p
                    (fnil conj [])
                    n))
          {}
          (for [n leaf-nsns
                p (nsn->all-parent-nsns-incl-self
                   n)]
            [n p])))

(defn ^:private nsn-permitted-by-user? [inclusions
                                        inclusions-re
                                        exclusions
                                        exclusions-re
                                        nsn]
  (let [inclusions-specified?    (seq inclusions)
        inclusions-re-specified? inclusions-re
        exclusions-specified?    (seq exclusions)
        exclusions-re-specified? exclusions-re]
    (letfn [(inclusions-ok-or-not-specified? []
              (or (not inclusions-specified?)
                  (some #(str/starts-with? nsn %) inclusions)))
            (inclusions-re-ok-or-not-specified? []
              (or (not inclusions-re-specified?)
                  (re-find (re-pattern inclusions-re) (name nsn))))
            (exclusions-ok-or-not-specified? []
              (or (not exclusions-specified?)
                  (not-any? #(str/starts-with? nsn %) exclusions)))
            (exclusions-re-ok-or-not-specified? []
              (or (not exclusions-re-specified?)
                  (not (re-find (re-pattern exclusions-re) (name nsn)))))]
      (and (exclusions-ok-or-not-specified?)
           (exclusions-re-ok-or-not-specified?)
           (cond (and inclusions-specified?
                      inclusions-re-specified?)
                 (or (inclusions-ok-or-not-specified?)
                     (inclusions-re-ok-or-not-specified?))
                 ;;
                 inclusions-specified?
                 (inclusions-ok-or-not-specified?)
                 ;;
                 inclusions-re-specified?
                 (inclusions-re-ok-or-not-specified?)
                 ;;
                 :else
                 true)))))

(defn ^:private make-permit-nsn?-fun
  ;; "permit" rather than "include" because that makes for easy
  ;; grepping of:
  ;; - incl -- include / including / inclusion
  ;; - excl -- exclude / excluding / inclusion
  ;; - permit (meaning include and not exclude)
  [nsn->part-of-project?
   inclusions
   inclusions-re
   exclusions
   exclusions-re
   show-non-project-deps]
  (fn [nsn]
    (and (or (nsn->part-of-project? nsn)
             show-non-project-deps)
         (nsn-permitted-by-user? inclusions
                                 inclusions-re
                                 exclusions
                                 exclusions-re
                                 nsn))))

;;;; ___________________________________________________________________________
;;;; Make dot data

(defn ^:private ns-graph-spec->title-dot-data [{:keys [platform
                                                       source-paths
                                                       inclusions
                                                       inclusions-re
                                                       exclusions
                                                       exclusions-re
                                                       show-non-project-deps
                                                       project-group
                                                       project-name]
                                                :as ns-graph-spec}]
  (str (str (str project-group "/" project-name)
            " namespace dependencies")
       (str "\\l:platform: " (name platform))
       (str "\\l:source-paths: "
            (letfn [(fix-slashes [s]
                      (str/replace s "\\" "/"))]
              (let [root-path (str (-> (clojure.java.io/file ".")
                                       .getCanonicalPath
                                       fix-slashes)
                                   "/")]
                (str/join " "
                          (->> source-paths
                               (map fix-slashes)
                               (map #(str/replace-first %
                                                        root-path
                                                        "")))))))
       (when show-non-project-deps
         "\\l:show-non-project-deps true")
       (when-not (nil? inclusions-re)
         (str "\\l:inclusions-re: " inclusions-re))
       (when-not (empty? inclusions)
         (str "\\l:inclusions:\\l"
              (apply str
                     (str/join "\\l"
                               (map (partial str "    ")
                                    inclusions)))))
       (when-not (nil? exclusions-re)
         (str "\\l:exclusions-re: " exclusions-re))
       (when-not (empty? exclusions)
         (str "\\l:exclusions:\\l"
              (apply str
                     (str/join "\\l"
                               (map (partial str "    ")
                                    exclusions)))))
       "\\l"))

(defn ^:private part-of-project?->style-map [part-of-project?]
  {:style (if part-of-project?
            :solid
            :dashed)})

(defn ^:private make-nsn->descriptor-fun [nsn->part-of-project?]
  (fn [nsn color]
    (merge (-> nsn
               nsn->part-of-project?
               part-of-project?->style-map)
           {:label (nsn->last-piece nsn)
            :color color
            :fontcolor color})))

(defn ^:private much-stuff->dot [nsns-to-show
                                 nsn->dependees
                                 nsn->part-of-project?
                                 nsn->has-dependees?
                                 nsn->has-dependers?
                                 ns-graph-spec]
  (let [nsn->descriptor-fun (-> nsn->part-of-project?
                                make-nsn->descriptor-fun)]
    (dot/graph->dot nsns-to-show
                    nsn->dependees
                    :node->descriptor #(nsn->descriptor-fun % :black)
                    :edge->descriptor #(-> %2
                                           nsn->part-of-project?
                                           part-of-project?->style-map)
                    :options {:dpi 300}
                    :cluster->show-as-node? #(or (nsn->has-dependees? %)
                                                 (nsn->has-dependers? %))
                    :cluster->descriptor #(nsn->descriptor-fun %
                                                               (case 1 ; for easy dev/debug
                                                                 1 :blue
                                                                 2 :red
                                                                 3 :purple))
                    :node->cluster nsn->parent-nsn
                    :cluster->parent nsn->parent-nsn
                    :left-justify-cluster-labels? true
                    :title (ns-graph-spec->title-dot-data ns-graph-spec))))

;;;; ___________________________________________________________________________
;;;; Glue it all together

(defn ns-graph-spec->dot-data [{:keys [platform
                                       source-paths
                                       inclusions
                                       inclusions-re
                                       exclusions
                                       exclusions-re
                                       show-non-project-deps
                                       project-group
                                       project-name]
                                :as ns-graph-spec}]
  (let [source-files           (compute-source-files platform
                                                     source-paths)
        dep-graph              (source-files->dep-graph platform
                                                        source-files)
        nsns-with-parents      (-> source-files
                                   source-files->nsns
                                   nsns->all-nsns)
        nsn->part-of-project?  (partial contains? nsns-with-parents)
        permit-nsn?-fun        (make-permit-nsn?-fun nsn->part-of-project?
                                                     inclusions
                                                     inclusions-re
                                                     exclusions
                                                     exclusions-re
                                                     show-non-project-deps)
        leaf-nsns              (filter permit-nsn?-fun
                                       (ctns-dep/nodes dep-graph))
        nsns                   (nsns->all-nsns leaf-nsns)
        nsn->self-and-children (leaf-nsns->nsn->self-and-children leaf-nsns)
        nsn->dependees         #(filter permit-nsn?-fun
                                        (ctns-dep/immediate-dependencies
                                         dep-graph
                                         %))
        nsn->dependers         (u/invert-relation nsn->dependees
                                                  nsns)
        nsn->has-dependees?    (comp not
                                     empty?
                                     nsn->dependees)
        nsn->has-dependers?    (comp not
                                     empty?
                                     nsn->dependers)
        nsns-to-show           (filter (fn [n]
                                         (or (nsn->part-of-project? n)
                                             (some nsn->has-dependers?
                                                   (nsn->self-and-children n))))
                                       nsns)
        dot-data               (much-stuff->dot nsns-to-show
                                                nsn->dependees
                                                nsn->part-of-project?
                                                nsn->has-dependees?
                                                nsn->has-dependers?
                                                ns-graph-spec)]
    dot-data))
