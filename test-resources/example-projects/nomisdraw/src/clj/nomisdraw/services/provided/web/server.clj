(ns nomisdraw.services.provided.web.server
  (:require [clojure.pprint :as pp]
            [com.stuartsierra.component :as component]
            [ring.adapter.jetty :as jetty]
            [taoensso.timbre :as timbre]))

(defn ^:private make-jetty-server-and-run-it [handler port]
  (jetty/run-jetty handler
                   {:port  port
                    :join? false}))

;;;; ___________________________________________________________________________
;;;; Protocols and componentry

(defrecord ^:private WebServer [;; Injected
                                port
                                handler
                                ;; Added here
                                jetty-webserver]

  component/Lifecycle

  (start [this]
    (if jetty-webserver
      this
      (do
        (timbre/info "Starting webserver on port" port)
        (let [jw (try
                   (make-jetty-server-and-run-it handler
                                                 port)
                   (catch java.net.BindException e
                     (throw (Exception.
                             (pp/cl-format nil
                                           "Failed to run Jetty on port ~A.~@
                                            ~A"
                                           port e)))))]
          (assoc this :jetty-webserver jw)))))

  (stop [this]
    (if-not jetty-webserver
      this
      (do
        (timbre/info "Stopping webserver")
        (.stop jetty-webserver)
        (assoc this :jetty-webserver nil)))))

(defn make-webserver []
  (map->WebServer {}))
