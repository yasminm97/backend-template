(ns com.iraqidata.tablecloth
  (:require [tablecloth.api :as tc]))

;; Loading data from a CSV file
(tc/dataset "./resources/data/flights.csv")

;; Bind to a var
(def ds (tc/dataset "./resources/data/flights.csv"
                    {:key-fn keyword}))

;; Information about the dataset
(tc/info ds)

;; Column names
(tc/column-names ds)

;;  flights |>
;; filter(dest == "IAH") |>
;; group_by(year, month, day) |>
;; summarize(
;;   arr_delay = mean(arr_delay, na.rm = TRUE)
;; )

#_(kind/code "")

(-> ds
    (tc/select-rows (fn [row]
                      (= (:dest row)
                         "IAH")))
    (tc/group-by [:year :month :day])
    (tc/mean :arr_delay))
