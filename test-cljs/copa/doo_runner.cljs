(ns copa.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [copa.core-test]))

(doo-tests 'copa.core-test)

