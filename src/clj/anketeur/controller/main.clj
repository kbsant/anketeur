(ns anketeur.controller.main
  (:require
    [ashikasoft.webstack.layout :as layout]
    [anketeur.view.main :as view.main]))

(defn render [env]
  (layout/render-hiccup view.main/render {:glossary {:appname "Anketeur" :title "Survey" :message (:app-message env)}}))
