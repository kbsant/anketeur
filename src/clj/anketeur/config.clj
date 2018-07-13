(ns anketeur.config
  (:require [cprop.core :refer [load-config]]
            [cprop.source :as source]
            [integrant.core :as ig]))

(defmethod ig/init-key :anketeurweb/env [_ {:keys [args]}]
  (load-config
     :merge
     [args
      (source/from-system-props)
      (source/from-env)]))
#_
(defstate env :start (load-config
                       :merge
                       [(args)
                        (source/from-system-props)
                        (source/from-env)]))
