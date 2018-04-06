(ns ^:figwheel-no-load anketeur.app
  (:require
    [anketeur.core :as core]
    [anketeur.opener :as opener]
    [anketeur.client.result :as result]
    [anketeur.responder :as responder]
    [devtools.core :as devtools]))

(enable-console-print!)
(devtools/install!)
