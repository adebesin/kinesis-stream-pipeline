(ns pipeline.bridge
  (:gen-class
    :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.string :as str])
  (:import (java.io InputStream OutputStream OutputStreamWriter)
           (com.amazonaws.services.lambda.runtime Context)
           (com.amazonaws.services.stepfunctions AWSStepFunctionsAsyncClientBuilder)
           (com.amazonaws.services.stepfunctions.model StartExecutionRequest)))

(def state-machine-arn (System/getenv "STATE_MACHINE_ARN"))

(defn -handleRequest
  [this ^InputStream input-stream ^OutputStream output-stream ^Context context]
  (let [w (io/writer output-stream)
        parameters (json/read (io/reader input-stream) :key-fn keyword)]
    (println ">>> Initiating step functions")
    (println ">>> Parameters " parameters)
    (println ">>> SM ARN" state-machine-arn)
    (.get
      (.startExecutionAsync
        (AWSStepFunctionsAsyncClientBuilder/defaultClient)  ; AWSStepFunctionsAsyncClientBuilder is depreciated?
        (doto
          (StartExecutionRequest.)
          (.withStateMachineArn state-machine-arn)
          (.withInput (json/write-str parameters)))))

    ;(println ">>> Finished?" (.isDone res))
    ;(println ">>> ARN?" (.getExecutionArn (.get res)))

    ))
