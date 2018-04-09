(ns anketeur.layout
  (:require
    [anketeur.view.error :as view.error]
    [ring.util.http-response :refer [content-type ok]]
    [ring.util.anti-forgery :refer [anti-forgery-field]]
    [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]))

(declare ^:dynamic *app-context*)

(defn render-text [text]
  (content-type (ok text) "text/plain"))

(defn render-hiccup [hiccup-fn params]
  (content-type
    (ok
      (hiccup-fn
        (assoc params
           :csrf-token *anti-forgery-token*
           :servlet-context *app-context*)))
    "text/html; charset=utf-8"))

(defn error-page
  "error-details should be a map containing the following keys:
   :status - error status
   :title - error title (optional)
   :message - detailed error message (optional)

   returns a response map with the error page as the body
   and the status specified by the status key"
  [error-details]
  {:status  (:status error-details)
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    (view.error/render {:glossary error-details})})
