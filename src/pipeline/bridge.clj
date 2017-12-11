(ns pipeline.bridge
  (:gen-class
    :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [pipeline.lib :as lib])
  (:import (java.io InputStream OutputStream OutputStreamWriter)
           (com.amazonaws.services.lambda.runtime Context)
           (java.util.concurrent TimeUnit TimeoutException)))

;TODO Add docs to functions
;TODO Return the result of the StartExecutionResult in the json response returned by (trigger-step-functions ...) eg (.getStartTime res)

(def state-machine-arn (System/getenv "STATE_MACHINE_ARN"))

(defn -handleRequest
  [this ^InputStream input-stream ^OutputStream output-stream ^Context context]
  (let
    [writer (io/writer output-stream)
     blob (slurp (io/reader input-stream))]
    (println ">>> Initiating step functions")
    (println ">>> Blob " blob)
    (try
      (.get (lib/trigger-step-functions state-machine-arn blob)
            10
            TimeUnit/SECONDS)
      (.write writer (json/generate-string
                       {:Content-Type "application/json"
                        :statusCode   200
                        :body         {:message "OK"}}))
      (catch TimeoutException toe
        (.write writer (json/generate-string
                         {:Content-Type "application/json"
                          :statusCode   401
                          :body         {:message "ERROR"}}))
        (str ">>> " (.getMessage toe)))
      (finally (.flush writer)))))


