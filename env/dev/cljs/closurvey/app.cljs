(ns ^:figwheel-no-load closurvey.app
  (:require
    [closurvey.core :as core]
    [closurvey.opener :as opener]
    [closurvey.client.result :as result]
    [closurvey.responder :as responder]
    [devtools.core :as devtools]))

(enable-console-print!)
(devtools/install!)
