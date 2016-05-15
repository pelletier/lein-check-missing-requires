(ns leiningen.check-missing-requires
  (:require [lein-check-missing-requires.core :as core]))

(defn check-missing-requires
  "I don't do a lot."
  [project & args]
  (core/check-missing-requires-in-paths (:source-paths project)))
