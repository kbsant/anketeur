(ns anketeur.app
  (:require
    [anketeur.client.edit :as edit]
    [anketeur.client.opener :as opener]
    [anketeur.client.result :as result]
    [anketeur.client.responder :as responder]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))
