(ns com.iraqidata.tablecloth
  (:require
   [scicloj.kindly.v4.kind :as kind]
   [tablecloth.api :as tc]
   tablecloth.column.api.operators
   [tablecloth.column.api.operators :as operators]))

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

(kind/md "```r
flights |>
  arrange(desc(dep_delay))
```")

;; Clojure Code

(-> ds
    (tc/order-by :dep_delay))

;; ## Uniqueness

;; R Code

;; Removes duplicate rows.

(kind/md "```r
flights |>
  distinct()
```")

;; Clojure Code

;; Requires at least one column to be specified.  Keeps all other columns by default unlike R.

(-> ds
    (tc/unique-by :origin))

;; In R, you would use something like this
(kind/md "```r
flights |>
  count(origin, dest, sort = TRUE)
```")

;; To count rows and sort at the same time, this kind of **complex** code does
;; not occur in Clojure.

(-> ds
    (tc/group-by [:origin :dest])
    (tc/aggregate {:n tc/row-count})
    (tc/order-by :n :desc))

;; Notice how each function does one thing at a time, the grouping is ommited in
;; the R example and so is the naming of the new column.

;; You do not tack on arguments or hope that they exist for every single
;; function, you **simply** thread through another function.

;; ## Column Operations

;; ### Creating new columns

(kind/md "```r
flights |>
  mutate(
    gain = dep_delay - arr_delay,
    speed = distance / air_time * 60
  )
```")

(-> ds
    (tc/add-column :gain (:gain (tc/+ ds :gain [:dep_delay :arr_delay])))
    (tc/add-column :speed (:speed (tc// ds :speed [:distance :air_time])))

    (tc/head)
    (kind/table))

(-> ds
    (tc/add-columns {:gain (:gain (tc/+ ds :gain [:dep_delay :arr_delay]))
                     :speed (:speed (tc// ds :speed [:distance :air_time]))})
    (tc/update-columns :speed
                       (fn [column]
                         (operators/* column
                                      60)))
    (tc/head)
    (kind/table))

;; ### Selecting columns

;; ### Renaming columns

;; ### Moving columns around

;; # Threading vs Piping

;; ## Peek: Transducers

;; Do you ever think, hey why are we creating all those intermediate results?
