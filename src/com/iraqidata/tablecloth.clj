(ns com.iraqidata.tablecloth
  (:require [tablecloth.api :as tc]))

;; Loading data from a CSV file
(tc/dataset "./resources/data/flights.csv")

;; Bind to a var
(def ds (tc/dataset "./resources/data/flights.csv"))

;; Information about the dataset
(tc/info ds)

;; Column names
(tc/column-names ds)

;; Translate to tablecloth
;;  flights |>
;; filter(dest == "IAH") |>
;; group_by(year, month, day) |>
;; summarize(
;;   arr_delay = mean(arr_delay, na.rm = TRUE)
;; )
(-> ds
    (tc/select-rows (fn [row]
                      (= (get row "dest")
                         "IAH")))
    (tc/group-by ["year" "month" "day"])
    (tc/ {:arr_delay (tc/mean :arr_delay)}))
