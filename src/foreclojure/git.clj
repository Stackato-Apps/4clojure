(ns foreclojure.git
  (:require [clojure.string :as s])
  (:use [clojure.java.shell :only [sh]]
        [foreclojure.config :only [running-in-cloud?]]))

(letfn [(cmd [& args]
          (not-empty (s/trim (:out (apply sh args)))))]

  ;; fetch these at load time rather than on demand, so that it's accurate even
  ;; if someone checks out a different revision to poke at without restarting
  ;; the server (eg to diagnose bugs in a release)
  (def sha (if (running-in-cloud?) nil (cmd "git" "rev-parse" "--verify" "HEAD")))
  (def tag (if (running-in-cloud?) nil (cmd "git" "describe" "--abbrev=0" "master"))))
