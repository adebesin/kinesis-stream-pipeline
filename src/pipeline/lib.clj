(ns pipeline.lib
  (:require [clojure.java.io :as io]
            [cheshire.core :as json])
  (:import (java.util Base64)
           (java.io InputStream)
           (com.amazonaws.services.sns.model PublishRequest)
           (com.amazonaws.regions Region Regions)
           (com.amazonaws.auth ClasspathPropertiesFileCredentialsProvider)
           (com.amazonaws.services.sns AmazonSNSAsyncClientBuilder)
           (com.amazonaws.services.stepfunctions AWSStepFunctionsAsyncClientBuilder)
           (com.amazonaws.services.stepfunctions.model StartExecutionRequest)
           (java.util.concurrent TimeUnit TimeoutException FutureTask)))

;TODO Wrap the objects in protocols? or try stuart sierras library
;TODO create a real life json schema that i can build logic around which can be published to kinesis
;TODO create business logic function that defines whether a record should be sent to sns or not. Base it on whether the schema is correct. Add same logic to second lambda for extra retries

(defn ^FutureTask trigger-step-functions
  [state-machine-arn blob]
  (.startExecutionAsync
    (AWSStepFunctionsAsyncClientBuilder/defaultClient)
    (doto
      (StartExecutionRequest.)
      (.withStateMachineArn state-machine-arn)
      (.withInput blob))))

(defn extract-blob
  [event]
  (get-in event [:kinesis :data]))

(defn has-message? [^String message]
  (if (or (empty? message) (nil? message)) false true))

(defn parse-stream
  [^InputStream input-stream]
  (println ">>> Parsing Kinesis stream")
  (try
    (json/parse-stream
      (io/reader input-stream)
      true)
    (catch Exception error
      (println "[STREAM_PARSE_ERROR] - *SKIPPED* :"
               input-stream
               (.getMessage error)))))

(defn decode-blob
  [^String blob]
  (println ">>> Decoding JSON blob")
  (try
    (String. (.. Base64 getDecoder (decode blob)))
    (catch Exception error
      (println "[BLOB_DECODE_ERROR] - *SKIPPED* :"
               blob
               (.getMessage error)))))

(defn parse-blob
  [^String blob]
  (println ">>> Parsing JSON blob")
  (try
    (json/parse-string blob true)
    (catch Exception error
      (println "[BLOB_PARSE_ERROR] - *SKIPPED* :"
               blob
               (.getMessage error)))))

(defn publish-sns [^String arn ^String message]
  (.publish (AmazonSNSAsyncClientBuilder/defaultClient)
            (PublishRequest. arn (json/generate-string message))))

(defn write-dynamo [^String foo]
  ;TODO implement
  )

(def blob-xform
  (comp (keep extract-blob)
        (keep decode-blob)
        (keep parse-blob)
        (filter has-message?)))
