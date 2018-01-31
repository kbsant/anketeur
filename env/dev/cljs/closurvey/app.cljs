(ns ^:figwheel-no-load closurvey.app
  (:require [closurvey.core :as core]
            [closurvey.opener :as opener]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(defn ^:export init_opener []
    (opener/init!))

(defn ^:export init_edit []
    (core/init!))
