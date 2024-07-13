(ns com.iraqidata.tablecloth
  (:require [tablecloth.api :as tc]
            [scicloj.kindly.v4.kind :as kind]))

;; Loading data from a CSV file
(tc/dataset "./resources/data/flights.csv")

;; Bind to a var
(def ds (tc/dataset "./resources/data/flights.csv"
                    {:key-fn keyword}))

;; Information about the dataset
(tc/info ds)

;; Column names
(tc/column-names ds)

;; This is the R Code

^:kindly/hide-code
(kind/code "flights |>
  filter(dest == 'IAH') |>
  group_by(year, month, day) |>
  summarize(
    arr_delay = mean(arr_delay, na.rm = TRUE)
  )
")

;; This is the Clojure code

(-> ds
    (tc/select-rows (fn [row]
                      (= (:dest row)
                         "IAH")))
    (tc/group-by [:year :month :day])
    (tc/mean :arr_delay))

;; ## Re-order things around

;; R Code
(kind/code "flights |>
  arrange(desc(dep_delay))")

;; Clojure Code

(-> ds
    (tc/reorder-columns [:year :month :day :dep_time])
    (tc/order-by :dep_delay))
