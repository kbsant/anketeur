(ns anketeur.config
  (:require [cprop.core :refer [load-config]]
            [cprop.source :as source]
            [integrant.core :as ig]))

(defmethod ig/init-key :anketeurweb/env [_ {:keys [options]}]
  (load-config
     :merge
     [options
      (source/from-system-props)
      (source/from-env)]))

