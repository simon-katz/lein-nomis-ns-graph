(ns nomisdraw.system.system
  (:require [com.stuartsierra.component :as component]
            [nomisdraw.services.provided.web.handlers.handler :as handler]
            [nomisdraw.services.provided.web.server :as web.server]
            [taoensso.timbre :as timbre]))

;;;; ___________________________________________________________________________

(defn make-system
  "Returns a new instance of the whole application."
  [config]
  (component/system-map

   :handler (handler/make-handler)

   :port (get-in config [:http :port])

   :webserver (component/using (web.server/make-webserver)
                               [:port
                                :handler])))
