(ns nomisdraw.system.main
  (:require [com.stuartsierra.component :as component]
            [environ.core :as env]
            [nomisdraw.system.system :as system])
  (:gen-class))

;;;; ___________________________________________________________________________

(defn ^:private http-port-from-env []
  (when-let [port-as-string (env/env :nomisdraw-http-port)]
    (try (Integer/parseInt port-as-string)
         (catch NumberFormatException e
           (throw (RuntimeException.
                   (str ":nomisdraw-http-port in environment cannot be parsed: "
                        e)))))))

(def ^:private config
  {:http {:port (or (http-port-from-env)
                    26741)}})

;;;; ___________________________________________________________________________

(defn -main
  [& args]
  (assert (= (count args) 0)
          (str "Expected no args but got: " args))
  (component/start (system/make-system config)))
