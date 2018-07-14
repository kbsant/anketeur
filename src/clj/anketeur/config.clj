(ns anketeur.config
  (:require [cprop.core :as cprop]
            [cprop.source :as source]
            [integrant.core :as ig]))

(defn configure [options]
  "Configure the app dependency map, to be loaded by integrant."
  {:anketeurweb/env {:options options}
   :anketeurweb/ds {:env (ig/ref :anketeurweb/env)}
   :anketeurweb/httpd {:env (ig/ref :anketeurweb/env)
                       :ds (ig/ref :anketeurweb/ds)}
   #_ #_ :anketeurweb/nrepl {:env (ig/ref :anketeurweb/env)}})

(defmethod ig/init-key :anketeurweb/env [_ {:keys [options]}]
  (cprop/load-config
     :merge
     [options
      (source/from-system-props)
      (source/from-env)]))

