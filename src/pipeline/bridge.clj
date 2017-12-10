(ns pipeline.bridge
  (:gen-class
    :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [clojure.string :as str]
            [pipeline.lib :as lib])
  (:import (java.io InputStream OutputStream OutputStreamWriter)
           (com.amazonaws.services.lambda.runtime Context)
           (com.amazonaws.services.stepfunctions AWSStepFunctionsAsyncClientBuilder)
           (com.amazonaws.services.stepfunctions.model StartExecutionRequest)
           (java.util.concurrent TimeUnit TimeoutException)))

(def state-machine-arn (System/getenv "STATE_MACHINE_ARN"))

(defn -handleRequest
  [this ^InputStream input-stream ^OutputStream output-stream ^Context context]
  (let
    [os (io/writer output-stream)
     blob (slurp (io/reader input-stream))]
    (println ">>> Initiating step functions")
    (println ">>> Blob " blob)
    (try
      (do
        (.get (lib/trigger-step-functions state-machine-arn blob)
              10
              TimeUnit/SECONDS)
        (.write os
                ^String (json/generate-string
                          {:Content-Type "application/json"
                           :statusCode   200
                           :body         {:message "OK"}})))
      (catch TimeoutException toe
        (do
          (.write os
                  ^String (json/generate-string
                            {:Content-Type "application/json"
                             :statusCode   401
                             :body         {:message "FAIL"}}))
          (str ">>> " (.getMessage toe))))
      (finally (.flush os)))))


