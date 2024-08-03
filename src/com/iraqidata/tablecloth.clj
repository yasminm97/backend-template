(ns com.iraqidata.tablecloth
  (:require
   [clojure.string :as str]
   scicloj.clay.v2.api
   [scicloj.kindly.v4.kind :as kind]
   [tablecloth.api :as tc]
   tech.v3.datatype.casting))

(comment
  (scicloj.clay.v2.api/make! {:source-path "src/com/iraqidata/tablecloth.clj"
                              :format [:quarto :html]
                              :run-quarto false
                              :show false}))

(defn table
  "Custom table view."
  [ds]
  (kind/table ds
              {:element/max-height "300px"}))

;; # Loading & Inspecting Data

;; Loading data from a CSV file
(-> (tc/dataset "./resources/data/flights.csv")
    tc/head
    table)

;; Let's bind the data
(def ds (tc/dataset "./resources/data/flights.csv"
                    {:key-fn #(keyword (str/replace (name %) "_" "-"))}))

;; Information about the dataset
(-> (tc/info ds)
    table)

;; Column names
(tc/column-names ds)

;; ## Loading CSV in Python with Pandas

(kind/md "```{python}
import pandas as pd
df = pd.read_csv(\"../resources/data/flights.csv\")
df.head()
```")

;; ## Loading the library in R

;; I opted to using the existing library for brevity.

(kind/md "```{r}
library(dplyr)
library(nycflights13)
```
")

;; # Row Operations

;; ## Selecting Rows

;; Basic selection with a basic filter.

;; ### R
(kind/md "```{r}
flights |>
     filter(dest == 'IAH')
```
")

;; ### Python

(kind/md "```{python}
df[df['dest'] == 'IAH']
```")

;; ### Clojure

(-> ds
    (tc/select-rows (fn [row]
                      (= (:dest row)
                         "IAH"))))

;; ## Summarizing Rows

;; Let's select all the rows where the destination is IAH then group by the
;; year, month and day and calculate the average arrival delay.

;; ### R

(kind/md "```{r}
flights |>
  filter(dest == 'IAH') |>
  group_by(year, month, day) |>
  summarize(
    arr_delay = mean(arr_delay, na.rm = TRUE)
  )
```")

;; ### Clojure

(-> ds
    (tc/select-rows (fn [row]
                      (= (:dest row)
                         "IAH")))
    (tc/group-by [:year :month :day])
    (tc/mean :arr-delay))

;; ### Python

(kind/md "```{python}
filtered_flights = df[df['dest'] == 'IAH']

result = filtered_flights \\
.groupby(['year', 'month', 'day']) \\
.agg({'arr_delay': lambda x: x.mean(skipna=True)}) \\
.reset_index()

result
```
")


;; TODO: Should we show just the head from all 3?

;; ## Sort rows by column

;; ### R

(kind/md "```{r}
flights |>
  arrange(desc(dep_delay))
```")

;; ### Clojure

(-> ds
    (tc/order-by :dep-delay :desc))

;; ### Python

(kind/md "```{python}
df.sort_values(by='dep_delay', ascending=False)
```")

;; ## Unique Rows

;; ### R

;; R can do a distinct on all columns by default.  Clojure requires at least one
;; column to be specified.  When one column in R is specified it does not keep
;; all other column by default while Clojure does.

(kind/md "```{r}
flights |>
  distinct(c(origin), .keep_all = TRUE)
```")

;; ### Clojure

(-> ds
    (tc/unique-by :origin))

;; ### Python

;; unique by the origin column
(kind/md "```{python}
df.drop_duplicates(subset=['origin'])
```")

;; ## Counting Rows

;; Let's count the rows for the origin, destination pairs.

;; Change.

;; ### R

(kind/md "```{r}
flights |>
  count(origin, dest, sort = TRUE)
```")

;; ### Python

(kind/md "```{python}
df.groupby(['origin', 'dest']).size().sort_values(ascending=False)
```
")

;; ### Clojure

;; To count rows and sort at the same time, this kind of **complex** code does
;; not occur in Clojure.

(-> ds
    (tc/group-by [:origin :dest])
    (tc/aggregate {:n tc/row-count})
    (tc/order-by :n :desc))

;; Notice how each function does one thing at a time, the grouping is omitted in
;; the R example and so is the naming of the new column.

;; You do not tack on arguments or hope that they exist for every single
;; function, you **simply** thread through another function.

;; # Column Operations

;; ## Creating new columns

;; ### R

(kind/md "```{r}
flights |>
  mutate(
    gain = dep_delay - arr_delay,
  )
```")

;; ### Clojure

(comment
  (-> ds
      (tc/+ :gain [:dep-delay :arr-delay])
      :gain))

(-> ds
    (tc/add-column :gain (-> ds
                             (tc/+ :gain [:dep-delay :arr-delay])
                             :gain)))

;; ### Python

;; TODO: Mutable

(kind/md "```{python}
df['gain'] = df['dep_delay'] - df['arr_delay']

df
```")

;; ## Selecting columns

;; ### R

(kind/md "```{r}
flights |>
  select(year, month, day)

flights |>
  select(year:day)
```")

;; ### Clojure

(-> ds
    (tc/select-columns [:year :month :day]))

;; Instead of negating a selecting, thus complicating the operation, we simply drop columns

;; TODO: Move down? Separate section?

(-> ds
    (tc/drop-columns [:year :month :day]))

;; **There is no notion of ranges in column selection by default.**

;; What if you really, **really** wanted a range selector for columns?

;; It's trivial to write such helper functions in Clojure due to its amazing
;; data structures.

;; TODO: Explain step-by-step

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

;; ### Python

(kind/md "```{python}
df.loc[:, 'year':'day']
```")

;; ## Selecting based on type

;; ### R

(kind/md "```{r}
flights |>
  select(where(is.character))
```")

(kind/md "```{r}
flights |>
  select(where(is.numeric))
```")

;; ### Clojure

;; We can select based on the type directly.

(-> ds
    (tc/select-columns :type/numerical))

;; TODO: Report to Daniel.

(-> ds
    (tc/select-columns :type/string))

;; What are the possible types?

(table
 {:type (tech.v3.datatype.casting/all-datatypes)})

;; ### Python

;; Select numbers only.

(kind/md "```{python}
df.select_dtypes(include=['number'])
```")

;; Select string only.

;; `object` is `string` ?

(kind/md "```{python}
df.select_dtypes(include=['object'])
```")

;; ## Renaming columns

;; ### R

;; Simple mapping from old name to new name.

(kind/md "```{r}
flights |>
  rename(tail_num = tailnum)
```")

;; ### Clojure

(-> ds
    (tc/rename-columns {:tail-num :tailnum}))

;; ### Python

(kind/md "```{python}
df.rename(columns={'tail_num': 'tailnum'}, inplace=True)

df['tailnum']
```
")

;; ## Moving columns around

;; ### R

(kind/md "```{r}
flights |>
  relocate(time_hour, air_time)
```")

;; ### Clojure

(-> ds
    (tc/reorder-columns [:time-hour :air-time])
    tc/head
    table)

;; ### Python

(kind/md "```{python}
df = df[['time_hour', 'air_time'] + [col for col in df.columns if col not in ['time_hour', 'air_time']]]

df
```
")

;; There is no equivalent to the `.after` and `.before` argument.

;; ## Moving columns around based on a condition.

;; ### R

(kind/md "```{r}
flights |>
  relocate(starts_with(\"arr\"))
```")

;; ### Clojure

;; Apply filtering using functions.

;; Relocation based on a condition is simple.

;; We use `(name column)` because a column contains data and we only want to
;; filter by the name.

(-> ds
    (tc/reorder-columns (fn [column]
                          (str/starts-with? (name column)
                                            "arr")))
    tc/head
    table)

;; ### Python

;; Identify columns that start with 'arr'

(kind/md "```{python}
arr_columns = [col for col in df.columns if col.startswith('arr')]
```
")

;; Identify other columns

(kind/md "```{python}
other_columns = [col for col in df.columns if not col.startswith('arr')]
```
")

;; Reorder columns: 'arr' columns first, then other columns

(kind/md "```{python}
df_reorder = df[arr_columns + other_columns]
df_reorder.head()
```
")

;; # Threading, Piping

;; ## Threading in Clojure

;; Clojure supports an operation similar to piping in R, it's called threading
;; with multiple variations.

;; ### Thread-first

;; The most common threading macro and the match to R's `|>` pipe operator.  It
;; places its argument as the first argument of every subsequent function call.

(/ (+ 4 3) 4.0)

(-> 4
    (+ 3)
    (/ 4.0))

;; One way to visualize this

(-> 4
    (+ ,,, 3)
    (/ ,,, 4.0))

(-> {:name "Ali"}
    (assoc :age 30)
    (update :age inc))

;; ### Thread-last

;; Let's consider the case of the following problem, the sum of the squares of
;; all even positive integers below ten.

;; Your first instinct might be
(reduce +
        (map #(* % %)
             (filter even?
                     (range 10))))

;; Which is correct but it's not as readable as the following

(->> (range 10)
     (filter even?)
     (map #(* % %))
     (reduce +))

(->> (range 10)
     (filter even?)
     (map #(* % %))
     (reduce +))

;; In R this form would look like this

(kind/md "```{r}
sum((seq(2, 8, by = 2))^2)
```")

;; #### Simple and Complex again

;; ::: {.grid}
;; ::: {.g-col-6}

;; Which might look easier to write but is far more complex, what if you want to
;; extend it by multiplying each number by 3.5?  Such operations are far simpler
;; to implement in Clojure because they mostly involve adding another function
;; call to the thread.

(->> (range 10)
     (filter even?)
     (map #(* % 3.5))
     (map #(* % %))
     (reduce +))

;; :::
;; ::: {.g-col-6}

;; Equivalent R code, with decreasing readability and increasing complexity.

(kind/md "```{r}
sum((seq(2, 8, by = 2) * 3.5)^2)
```")

;; :::
;; :::

;; ### Thread-as

;; Thread-as `as->` gives you complete freedom in where you place the arguments in the pipeline!

(as-> [:foo :bar] v
  (map name v)
  (first v)
  (.substring v 1))

;; No equivalent in R.

;; ## Peek: Transducers

;; Do you ever think, hey why are we creating all those intermediate results?

;; We can use a concept in Clojure
;; called [Transducers](https://clojure.org/reference/transducers) which simply
;; means applying all the functions at once without intermediate results!

(transduce
 ;; Composition of functions
 (comp (filter even?)
       (map #(* % 3.5))
       (map #(* % %)))
 ;; Function to reduce by.
 +
 ;; Collection to work over.
 (range 10000))

;; Time Comparison
^:kindly/hide-code
(table
 (tc/dataset {"Case"                  ["Transducers" "Threading" "Difference "]
              "Average time in ms"    (map #(format "%.5f" %)
                                           [0.47711006 0.5792014 (- 0.5792014 0.47711006)])}))

;; Around 10 msecs in the transducer case, which is a difference of %17.

;; # Grouping

;; ## `group-by`

;; The way grouping happens works differently between R and Clojure so I'll only
;; discuss Clojure's method here but provide mirror uses cases.

(-> ds
    (tc/group-by :month))

;; We see this produces smaller datasets that are the groups.

;; We can summarize such groups.

(-> ds
    (tc/group-by :month)
    (tc/mean :dep-delay)
    (tc/rename-columns [:month :mean])
    (tc/order-by :month))

;; Another method

(-> ds
    (tc/group-by :month)
    (tc/aggregate {:mean (fn [ds]
                           (get (tc/mean ds :dep-delay)
                                "summary"))})
    (tc/rename-columns [:month :mean])
    (tc/order-by :month))

;; Notice that we ignore missing values, similar code in R would break unless
;; you use `na.rm = TRUE`

;; ## Selecting from a group

;; There is no direct counterpart to the `slice_` functions in R, that's fine we
;; can reach their functionality with a one or a few basic functions.

;; `slice_head()`

;; Takes the first row of each group.

;; You can use `(tc/select-rows ds 0)`.

(-> ds
    (tc/group-by :month)
    (tc/select-rows 0)
    (tc/ungroup))

;; `slice_max()`

;; takes the row with the largest value of column `x`.

(-> ds
    (tc/group-by :dest)
    (tc/order-by :arr-delay :desc)
    (tc/select-rows 0)
    (tc/ungroup))

(defn slice-max
  "Select the row with the largest value of `column-kw`."
  [ds column-kw]
  (-> ds
      (tc/order-by column-kw :desc)
      (tc/select-rows 0)
      (tc/ungroup)))
(-> ds
    (tc/group-by :dest)
    (slice-max :arr-delay))

;; It's helpful to test our code.

(= (-> ds
       (tc/group-by :dest)
       (tc/order-by :arr-delay :desc)
       (tc/select-rows 0)
       (tc/ungroup))
   (-> ds
       (tc/group-by :dest)
       (slice-max :arr-delay)))

;; Lastly, this is a naive quick implementation that assumes a grouped
;; collection, there are ways to dispatch for various cases but that's outside
;; the scope at the moment.

;; `slice_sample`

;; For selecting any random row.

(kind/code
 "(-> ds
      (tc/select-rows (-> ds
                          tc/row-count
                          rand-int)))")

;; Per group requires a bit more code as we work through each group, one at a
;; time.

(kind/code "(as-> ds ds
  (tc/group-by ds :dest)
  (tc/order-by ds :arr_delay :desc)
  (tc/groups->seq ds)
  (map (fn [group]
         (tc/select-rows group
                         (-> group
                             tc/row-count
                             rand-int)))
       ds))")

;; # Problem

;; ## Average Delayed Flights

;; Let's find the average delay for each airline.

;; First, we need to load the airlines dataset.

(def airlines
  (tc/dataset "./resources/data/airlines.csv"
              {:key-fn keyword}))

(-> airlines
    tc/head)

(-> airlines
    tc/info)

;; Next we find the average delay for each airline.

;; We join to get the airline names, then group by the name and find the average.

(-> (tc/inner-join ds airlines [:carrier])
    (tc/group-by :name)
    (tc/mean :arr-delay)
    (tc/rename-columns [:airlines :mean])
    (tc/order-by :mean :desc)
    table)

;; What if we want to visualize this data as a chart?

;; Clay provides the notion of Kinds, which can be a kind of Highcharts, D3 and
;; many other libraries.  An extensive list can be [found here](https://scicloj.github.io/clay/clay_book.examples.html)

;; We can use the `highcharts` kind to visualize this data. The following code
;; was guided by the [Highcharts bar chart
;; example](https://www.highcharts.com/docs/chart-and-series-types/bar-chart).

(let [ds (-> (tc/inner-join ds airlines [:carrier])
             (tc/group-by :name)
             (tc/mean :arr-delay)
             (tc/rename-columns [:name :data])
             (tc/order-by :data :desc))]
  (kind/highcharts
   {:chart {:type "bar"}
    :title {:text "Airlines with the most delays"}
    :xAxis {:categories (-> ds
                            :name
                            vec)}
    :tooltip {:pointFormat "{series.name}: <b>{point.y:.1f}</b>"}
    :yAxis {:title {:text "Average Delay"}}
    :series [{:name "Average Delay"
              :data (-> ds
                        :data
                        vec)}]}))
