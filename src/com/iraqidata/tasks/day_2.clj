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

;; Correct, 1 full point

;; 2. How many unique carriers were there in total?
(def airlines-ds (tc/dataset "./resources/data/airlines.csv"
                             {:key-fn keyword}))

(def unique-carriers (-> airlines-ds
                         (tc/select-columns :carrier)
                         distinct
                         count))
(println "The total number of carriers is:" unique-carriers)

;; The code does not run. Zero. There was also no need to use `airlines.csv`.

;; The correct code is

(def unique-carriers-works (-> flights-ds
                               (tc/unique-by :carrier)
                               (tc/row-count)))
unique-carriers
;; => 16

;; 3. How many unique airports were there in total?
(def airports-ds (tc/dataset "./resources/data/airports.csv"
                             {:key-fn keyword}))

(def unique-airports (count (distinct (tc/select-columns airports-ds :name))))
(println "The total number of airports is:" unique-airports)

;; Again, the code does not run. Zero. There was no need to use `airports.csv` here.

;; 4. What is the average arrival delay for each month?
(def avg-arrival-delay-per-month
  (-> flights-ds
      (tc/group-by :month)
      (tc/aggregate {:arrival-delay :mean})
      (tc/select-columns :month :arrival-delay)))
(println "Average arrival delay for each month:" avg-arrival-delay-per-month)

;; The code runs but there is no result. Zero.

;; Optional: Use the `airlines` dataset to get the name of the carrier with the
;; highest average distance.
;; (def avg-distance-per-carrier
;;   (->> airlines-ds
;;        (tc/group-by :carrier)
;;        (tc/aggregate {:distance :mean})
;;        (tc/select-columns :carrier :distance)))

;; Final score: 1.
