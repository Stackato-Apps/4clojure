(ns foreclojure.config
  (:use [clojure.java.io :only [file]]
        [clj-config.core :only [safely read-config]]
        [cheshire.core :only (parse-string)]))

(defn- running-in-cloud? []
  (boolean (System/getenv "VCAP_APP_PORT")))

(defn- assoc-cloud-env
  "Import Cloud Foundry / Stackato environment settings"
  [config]
  (let [port (Integer/parseInt (System/getenv "VCAP_APP_PORT"))
        srv  (parse-string (System/getenv "VCAP_SERVICES"))
        cred ((first (srv "mongodb-1.8")) "credentials")]
    (assoc config
      :jetty-port port  
      :db-host    (cred "host")
      :db-port    (cred "port")
      :db-user    (cred "username")
      :db-pwd     (cred "password")
      :db-dbname  (cred "db"))))

(def config-file (file (System/getProperty "user.dir") "config.clj"))

(def config (let [config (safely read-config config-file)]
              (if (running-in-cloud?)
                (assoc-cloud-env config)
                config)))

;; Defs both for convenience and compile-time verification of simple settings
(def repo-url (or (:repo-url config)
                  (throw (Exception. "config.clj needs a :repo-url key"))))

(letfn [(host [key]
          (get-in config [:hosts key]))]
  (def static-host (host :static))
  (def dynamic-host (host :dynamic))
  (def redirect-hosts (host :redirects)))
