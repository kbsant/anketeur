(ns anketeur.layout
  (:require
    [anketeur.view.error :as view.error]
    [ring.util.http-response :refer [content-type ok]]
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
  [{:keys [status] :as details}]
  {:status  status
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    (view.error/render {:glossary details})})
