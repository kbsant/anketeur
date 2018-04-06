(ns anketeur.app
  (:require
    [anketeur.core :as core]
    [anketeur.opener :as opener]
    [anketeur.client.result :as result]
    [anketeur.responder :as responder]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))
