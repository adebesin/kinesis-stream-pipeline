(defproject pipeline "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-RC2"]
                 [com.amazonaws/aws-lambda-java-core "1.2.0"]
                 [com.amazonaws/aws-java-sdk-kinesis "1.11.238"]
                 [com.amazonaws/aws-java-sdk-sns "1.11.240"]
                 [org.clojure/data.json "0.2.6"]]
  :aot :all)
