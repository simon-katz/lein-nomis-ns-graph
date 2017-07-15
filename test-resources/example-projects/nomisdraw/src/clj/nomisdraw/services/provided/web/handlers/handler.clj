(ns nomisdraw.services.provided.web.handlers.handler
  (:require [compojure.core :refer [HEAD GET PUT POST defroutes]]
            [compojure.route :as route]
            [ring.middleware.format :as ring-format]
            [ring.middleware.stacktrace :as ringstack]
            [ring.util.response :as response]
            [taoensso.timbre :as timbre]))

;;;; ___________________________________________________________________________

(defn ^:private wrap-exception [handler]
  (fn [request]
    (try (handler request)
         (catch Exception e
           {:status 500
            :body (str e)}))))

(defn ^:private wrap-exception-and-log
  "Wrap a handler such that exceptions are logged to *err* and minimal
  information is given in the response.
  Accepts the following options:
  :color? if true, apply ANSI colors to stacktrace (default false)"
  {:arglists '([handler] [handler options])}
  [handler & [{color? :color?}]]
  (-> handler
      (ringstack/wrap-stacktrace-log {:color? color?})
      wrap-exception))

;;;; ___________________________________________________________________________

(defn ^:private make-handler* []
  (compojure.core/routes
   ;;
   (route/resources "/")
   ;;
   (GET "/something-from-the-server" []
     "==== A reply from the server")
   ;;
   (route/not-found "Page not found")))

(defn make-handler []
  (-> (make-handler*)
      (wrap-exception-and-log ; {:color? true}
       )))

;;;; ___________________________________________________________________________
;;;; Protocols and componentry
