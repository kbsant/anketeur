(ns anketeur.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [anketeur.core-test]))

(doo-tests 'anketeur.core-test)

