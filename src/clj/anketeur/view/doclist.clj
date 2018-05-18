(ns anketeur.view.doclist
  (:require
    [anketeur.form :as form]
    [anketeur.view.parts :as parts]))

(defn content [data]
  [:div.container
    (form/navbar [:a {:href "/"} "Home"])
    (form/open-doclist data)])

(defn render [{:keys [glossary] :as data}]
  (parts/main glossary 
    nil
    (content data)))

