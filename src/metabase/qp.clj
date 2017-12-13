(ns metabase.qp
  "Metabase Query Processor 4.0."
  (:require [metabase.driver :as driver]
            [metabase.mbql :as mbql]
            [metabase.mbql
             [parse :as parse]
             [resolve :as resolve]]
            [metabase.models.database :refer [Database]]
            [metabase.util :as u]
            [toucan.db :as db]))

(defn- resolve-database [query]
  (update query :database (comp Database u/get-id)))

(defn- resolve-driver [query]
  (assoc query :driver (driver/engine->driver (get-in query [:database :engine]))))

(defn- parse-query [query]
  (update query :query parse/parse))

(defn- resolve-query [query]
  (println "Parsed query:" (u/pprint-to-str 'cyan (:query query))) ; NOCOMMIT
  (update query :query resolve/resolve))

(defn- convert-to-native-query [query]
  (println "Resolved query:" (u/pprint-to-str 'yellow (:query query))) ; NOCOMMIT
  (assoc query :native (driver/mbql->native (:driver query) query)))

(defn- run-query [query]
  (println "Native query:" (u/pprint-to-str 'green (:native query))) ; NOCOMMIT
  (driver/execute-query (:driver query) query))

(defn- process-query [query]
  ;; TODO - catch exceptions
  (-> query
      resolve-database
      resolve-driver
      parse-query
      resolve-query
      convert-to-native-query
      run-query))

(defn- x []
  (process-query {:database 1, :type :query, :query {:source-table 1, :limit 2}}))
