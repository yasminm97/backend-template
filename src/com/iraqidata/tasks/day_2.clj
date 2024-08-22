(ns com.iraqidata.tasks.day-2
  (:require
   [clojure.string :as str]
   scicloj.clay.v2.api
   [tablecloth.api :as tc]
   tech.v3.datatype.casting))

(def flights-ds (tc/dataset "./resources/data/flights.csv"
                            {:key-fn #(keyword (str/replace (name %) "_" "-"))}))

;; 1. How many flights were there in total?
(def total-flights (tc/row-count flights-ds))
(println "The total number of flights is:" total-flights)

;; 2. How many unique carriers were there in total?
(def airlines-ds (tc/dataset "./resources/data/airlines.csv"
                             {:key-fn keyword}))

(def unique-carriers (count (distinct (tc/select-columns airlines-ds :carrier))))
(println "The total number of carriers is:" unique-carriers)

;; 3. How many unique airports were there in total?
(def airports-ds (tc/dataset "./resources/data/airports.csv"
                             {:key-fn keyword}))

(def unique-airports (count (distinct (tc/select-columns airports-ds :name))))
(println "The total number of airports is:" unique-airports)

;; 4. What is the average arrival delay for each month?
(def avg-arrival-delay-per-month
  (-> flights-ds
      (tc/group-by :month)
      (tc/aggregate {:arrival-delay :mean})
      (tc/select-columns :month :arrival-delay)))
(println "Average arrival delay for each month:" avg-arrival-delay-per-month)

;; Optional: Use the `airlines` dataset to get the name of the carrier with the
;; highest average distance.
;; (def avg-distance-per-carrier
;;   (->> airlines-ds
;;        (tc/group-by :carrier)                
;;        (tc/aggregate {:distance :mean})      
;;        (tc/select-columns :carrier :distance)))
