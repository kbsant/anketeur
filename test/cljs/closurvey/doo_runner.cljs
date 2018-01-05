(ns closurvey.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [closurvey.core-test]))

(doo-tests 'closurvey.core-test)

