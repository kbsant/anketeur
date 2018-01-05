(ns ^:figwheel-no-load closurvey.app
  (:require [closurvey.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(defn ^:export init_edit []
    (core/init!))
