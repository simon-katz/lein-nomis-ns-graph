(ns leiningen.nomis-ns-graph.p100-args-and-options.p200-options-test
  (:require [leiningen.lein-test-utils :as ltu]
            [leiningen.nomis-ns-graph.p100-args-and-options.p200-options :refer :all]
            [midje.sweet :refer :all]))

(def the-project
  {:group "the-group"
   :name  "the-name"
   :source-paths ["the" "project" "source" "paths"]})

(def options-for-augmentation
  {:show-non-project-deps "the-show-non-project-deps"
   :exclusions            "the-exclusions"
   :exclusions-re         "the-exclusions-re"
   :filename              "the-filename"
   :write-gv-file?        "the-write-gv-file?"})

(fact
  (ltu/with-ignore-logging []
    (project-&-command-line-options->specs the-project
                                           (assoc options-for-augmentation
                                                  :platform :clj)))
  =>
  {:ns-graph-spec {:platform              :clj
                   :source-paths          ["the" "project" "source" "paths"]
                   :exclusions            "the-exclusions"
                   :exclusions-re         "the-exclusions-re"
                   :show-non-project-deps "the-show-non-project-deps"
                   :project-group         "the-group"
                   :project-name          "the-name"}
   :output-spec {:filename       "the-filename"
                 :write-gv-file? "the-write-gv-file?"}})

(fact
  (ltu/with-ignore-logging []
    (project-&-command-line-options->specs the-project
                                           (assoc options-for-augmentation
                                                  :platform :clj
                                                  :source-paths ["the"
                                                                 "specs"
                                                                 "source"
                                                                 "paths"])))
  =>
  {:ns-graph-spec {:platform              :clj
                   :source-paths          ["the" "specs" "source" "paths"]
                   :exclusions            "the-exclusions"
                   :exclusions-re         "the-exclusions-re"
                   :show-non-project-deps "the-show-non-project-deps"
                   :project-group         "the-group"
                   :project-name          "the-name"}
   :output-spec {:filename       "the-filename"
                 :write-gv-file? "the-write-gv-file?"}})

(fact
  (ltu/with-ignore-logging []
    (project-&-command-line-options->specs the-project
                                           (assoc options-for-augmentation
                                                  :platform :cljs)))
  =>
  {:ns-graph-spec {:platform              :cljs
                   :source-paths          ["src/cljs" "cljs/src"]
                   :exclusions            "the-exclusions"
                   :exclusions-re         "the-exclusions-re"
                   :show-non-project-deps "the-show-non-project-deps"
                   :project-group         "the-group"
                   :project-name          "the-name"}
   :output-spec {:filename       "the-filename"
                 :write-gv-file? "the-write-gv-file?"}})

(fact
  (ltu/with-ignore-logging []
    (project-&-command-line-options->specs the-project
                                           (assoc options-for-augmentation
                                                  :platform :cljs
                                                  :source-paths ["the"
                                                                 "specs"
                                                                 "source"
                                                                 "paths"])))
  =>
  {:ns-graph-spec {:platform              :cljs
                   :source-paths          ["the" "specs" "source" "paths"]
                   :exclusions            "the-exclusions"
                   :exclusions-re         "the-exclusions-re"
                   :show-non-project-deps "the-show-non-project-deps"
                   :project-group         "the-group"
                   :project-name          "the-name"}
   :output-spec {:filename       "the-filename"
                 :write-gv-file? "the-write-gv-file?"}})
