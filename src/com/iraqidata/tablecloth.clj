(ns com.iraqidata.tablecloth
  (:require
   [clojure.string :as str]
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
    (tc/add-column :speed (:speed (tc// ds :speed [:distance :air_time]))))

(-> ds
    (tc/add-columns {:gain (:gain (tc/+ ds :gain [:dep_delay :arr_delay]))
                     :speed (:speed (tc// ds :speed [:distance :air_time]))})
    (tc/update-columns :speed
                       (fn [column]
                         (operators/* column
                                      60))))

;; ### Selecting columns

(kind/md "```r
flights |>
  select(year, month, day)

flights |>
  select(year:day)
```")

;; Clojure Code

(-> ds
    (tc/select-columns [:year :month :day]))

;; Instead of negating a selecting, thus complicating the operation, we simply drop columns

(-> ds
    (tc/drop-columns [:year :month :day]))

;; **There is no notion of ranges in column selection by default.**

;; What if you really, **really** wanted a range selector for columns?

;; It's trivial to write such helper functions in Clojure due to its amazing
;; data structures.

(defn select-range-columns
  [ds start end]
  (tc/select-columns ds
                     (subvec (into [] (tc/column-names ds))
                             (.indexOf (tc/column-names ds)
                                       start)
                             (inc (.indexOf (tc/column-names ds)
                                            end)))))

(-> ds
    (select-range-columns :year :day))

;; ### Selecting based on type

;; R

(kind/md "```r
flights |>
  select(where(is.character))
```")

;; We can select based on the type directly.

(-> ds
    (tc/select-columns :type/numerical))

(-> ds
    (tc/select-columns :type/string))

;; ### Renaming columns

(kind/md "```r
flights |>
  rename(tail_num = tailnum)
```")

(-> ds
    (tc/rename-columns {:tail_num :tailnum}))

;; ### Moving columns around

(kind/md "```r
flights |>
  relocate(time_hour, air_time)
```")

(-> ds
    (tc/reorder-columns [:time_hour :air_time]))

;; There is no equivelant to the `.after` and `.before` argument.

(kind/md "```r
flights |>
  relocate(year:dep_time, .after = time_hour)

flights |>
  relocate(starts_with(\"arr\"), .before = dep_time)
```")

;; Apply filtering using functions.

(-> ds
    (tc/reorder-columns (fn [column]
                          (str/starts-with? (name column)
                                            "arr"))))

;; # Threading, Piping

;; ## Threading in Clojure

;; Clojure supports an operation similar to piping in R, it's called threading
;; with multiple variations.

;; ### Thread-first

;; The most common threading macro and the match to R's `|>` pipe operator.  It
;; places the first argument as the first argument of every subsequent function
;; call.

(-> 4
    (+ 3)
    (/ 4.0))

(/ (+ 4 3) 4.0)

;; ### Thread-last

;; ### Thread-as

;; ## Peek: Transducers

;; Do you ever think, hey why are we creating all those intermediate results?
