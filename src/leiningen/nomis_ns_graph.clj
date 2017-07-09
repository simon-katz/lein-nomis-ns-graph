(ns leiningen.nomis-ns-graph
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.tools.namespace.dependency :as ns-dep]
            [clojure.tools.namespace.file :as ns-file]
            [clojure.tools.namespace.find :as ns-find]
            [clojure.tools.namespace.parse :as parse]
            [clojure.tools.namespace.track :as ns-track]
            [rhizome.viz :as viz])
  (:import [java.io PushbackReader]))

(defn- add-image-extension [name]
  (str name ".png"))

(defn- hash-user-arguments [args options]
  (try (apply hash-map args)
       (catch Exception e (do (println "WARNING: Optional argument missing a corresponding value. Defaulting."))
              options)))

(defn- build-arguments [args]
  (let [options {"-name"     "nomis-ns-graph"
                 "-platform" ":clj"}
        hashed-args (hash-user-arguments args options)
        valid-options (remove nil? (map #(find hashed-args (first %)) options))]
    (merge options (into {} (filter (comp some? val) valid-options)))))

(defn ns-symbol->pieces [sym]
  (as-> sym __
    (name __)
    (str/split __ #"\.")
    (map symbol __)))

(defn ns-symbol->last-piece [sym]
  (-> sym
      ns-symbol->pieces
      last))

(defn pieces->ns-symbol [pieces]
  (->> pieces
       (str/join ".")
       symbol))

(defn ns-symbol->parent-ns-symbol [sym]
  (as-> sym __
    (name __)
    (str/split __ #"\.")
    (butlast __)
    (str/join "." __)
    (if (= __ "")
      nil
      (symbol __))))

(defn ns-symbol->all-parent-ns-symbols-incl-self [sym]
  (as-> sym __
    (name __)
    (str/split __ #"\.")
    (iterate butlast __)
    (take-while (comp not nil?)
                __)
    (map pieces->ns-symbol __)))

(defn nomis-ns-graph
  "Create a namespace dependency graph and save it as either nomis-ns-graph or the supplied name."
  [project & args]
  (let [built-args (build-arguments args)
        filename (add-image-extension (get built-args "-name"))
        platform (case (edn/read-string (get built-args "-platform"))
                   :clj ns-find/clj
                   :cljs ns-find/cljs
                   ns-find/clj)
        source-files (apply set/union
                            (map (comp #(ns-find/find-sources-in-dir % platform)
                                       io/file)
                                 (project :source-paths)))
        tracker (ns-file/add-files {} source-files)
        dep-graph (tracker ::ns-track/deps)
        ns-names (set (map (comp second ns-file/read-file-ns-decl)
                           source-files))
        part-of-project? (partial contains? ns-names)
        leaf-nodes (filter part-of-project? (ns-dep/nodes dep-graph))
        nodes (apply set/union
                     (map (comp set ns-symbol->all-parent-ns-symbols-incl-self)
                          leaf-nodes))]
    (viz/save-graph
     nodes
     #(filter part-of-project? (ns-dep/immediate-dependencies dep-graph %))
     :node->descriptor (fn [x]
                         {:label (ns-symbol->last-piece x)
                          :color :black})
     :options {:dpi 72}
     :do-not-show-clusters-as-nodes? true
     :cluster->descriptor (fn [x]
                            {:label (ns-symbol->last-piece x)
                             :color :blue})
     :node->cluster ns-symbol->parent-ns-symbol
     :cluster->parent ns-symbol->parent-ns-symbol
     :filename filename)
    (println "Created" filename)))
