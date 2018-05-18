(ns ^:figwheel-no-load anketeur.app
  (:require
    [anketeur.client.edit :as edit]
    [anketeur.client.result :as result]
    [anketeur.client.responder :as responder]
    [devtools.core :as devtools]))

(enable-console-print!)
(devtools/install!)
