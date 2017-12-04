(ns pipeline.lib
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json])
  (:import (java.util Base64)
           (java.io InputStream)
           (com.amazonaws.services.sns.model PublishRequest)
           (com.amazonaws.regions Region Regions)
           (com.amazonaws.auth ClasspathPropertiesFileCredentialsProvider)
           (com.amazonaws.services.sns AmazonSNSAsyncClientBuilder)))

;TODO Wrap the objects in protocols? or try stuart sierras library
;TODO create a real life json schema that i can build logic around which can be published to kinesis
;TODO create business logic function that defines whether a record should be sent to sns or not. Base it on whether the schema is correct. Add same logic to second lambda for extra retries

(defn extract-records [records]
  (let [recs (:Records records)]
    (if (empty? recs)
      (println ">>> [RECORD_EMPTY_ERROR]" recs)
      recs)))

(defn extract-blob
  [event]
  (get-in event
          [:kinesis :data]))

(defn has-message? [^String message]
  (if (or (empty? message) (nil? message)) false true))

(defn parse-stream
  [^InputStream input-stream]
  (println ">>> Parsing Kinesis stream")
  (try
    (json/read
      (io/reader input-stream)
      :key-fn keyword)
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
    (json/read-str blob
                   :key-fn keyword)
    (catch Exception error
      (println "[BLOB_PARSE_ERROR] - *SKIPPED* :"
               blob
               (.getMessage error)))))

(defn publish-sns [^String arn ^String message]
  (let [sns-client (AmazonSNSAsyncClientBuilder/defaultClient)]
    (if (= message {:message "error"})
      (.publish sns-client
                (PublishRequest. arn (json/write-str message)))
      message)))

(def blob-xform
  (comp (keep extract-blob) (keep decode-blob) (keep parse-blob)
        (filter has-message?)))

(def dead-letter-xform (comp))
