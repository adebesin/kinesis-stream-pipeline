(ns pipeline.lib
  (:require [cheshire.core :as json])
  (:import (java.util Base64)
           (com.amazonaws.services.sns.model PublishRequest)
           (com.amazonaws.services.sns AmazonSNSAsyncClientBuilder)
           (com.amazonaws.services.stepfunctions AWSStepFunctionsAsyncClientBuilder)
           (com.amazonaws.services.stepfunctions.model StartExecutionRequest)
           (java.util.concurrent FutureTask)))

;TODO Wrap the objects in protocols? or try stuart sierras library
;TODO create a real life json schema that i can build logic around which can be published to kinesis
;TODO create business logic function that defines whether a record should be sent to sns or not. Base it on whether the schema is correct. Add same logic to second lambda for extra retries

(defn ^FutureTask trigger-step-functions
  [^String state-machine-arn ^String blob]
  (.startExecutionAsync
    (AWSStepFunctionsAsyncClientBuilder/defaultClient)
    (doto
      (StartExecutionRequest.)
      (.withStateMachineArn state-machine-arn)
      (.withInput blob))))

(defn publish-sns [^String arn ^String message]
  (.publish (AmazonSNSAsyncClientBuilder/defaultClient)
            (PublishRequest. arn (json/generate-string message))))


