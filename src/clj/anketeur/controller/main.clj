(ns anketeur.controller.main
  (:require
    [anketeur.config :refer [env]]
    [anketeur.layout :as layout]
    [anketeur.view.main :as view.main]))

(defn render []
  (layout/render-hiccup view.main/render {:glossary {:appname "Anketeur" :title "Survey" :message (:app-message env)}}))
