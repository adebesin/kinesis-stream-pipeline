(ns pipeline.bridge
  (:gen-class
    :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [clojure.string :as str])
  (:import (java.io InputStream OutputStream OutputStreamWriter)
           (com.amazonaws.services.lambda.runtime Context)
           (com.amazonaws.services.stepfunctions AWSStepFunctionsAsyncClientBuilder)
           (com.amazonaws.services.stepfunctions.model StartExecutionRequest)))

(def ^:const state-machine-arn (System/getenv "STATE_MACHINE_ARN"))

(defn -handleRequest
  [this ^InputStream input-stream ^OutputStream output-stream ^Context context]
  (let [w (io/writer output-stream)
        blob (slurp (io/reader input-stream))]
    (println ">>> Initiating step functions")
    (println ">>> Blob " blob)
    (println ">>> SM ARN" state-machine-arn)
    (.get
      (.startExecutionAsync
        (AWSStepFunctionsAsyncClientBuilder/defaultClient)  ; AWSStepFunctionsAsyncClientBuilder is depreciated?
        (doto
          (StartExecutionRequest.)
          (.withStateMachineArn state-machine-arn)
          (.withInput blob))))

    ;TODO Check if async finished successfully before returning ok?
    ;(println ">>> Finished?" (.isDone res))
    ;(println ">>> ARN?" (.getExecutionArn (.get res)))

    (.write w ^String (json/generate-string {:Content-Type "application/json" :statusCode 200 :body {:message "OK"}}))
    (.flush w)
    ))
