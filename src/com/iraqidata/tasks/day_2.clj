(ns com.iraqidata.tasks.day-2
  (:require
   [clojure.string :as str]
   scicloj.clay.v2.api
   [tablecloth.api :as tc]
   tech.v3.datatype.casting))

(def ds (tc/dataset "./resources/data/flights.csv"
                    {:key-fn #(keyword (str/replace (name %) "_" "-"))}))

;; 1. How many flights were there in total?


;; 2. How many unique carriers were there in total?


;; 3. How many unique airports were there in total?


;; 4. What is the average arrival delay for each month?


;; Optional: Use the `airlines` dataset to get the name of the carrier with the
;; highest average distance.

(def airlines
  (tc/dataset "./resources/data/airlines.csv"
              {:key-fn keyword}))
