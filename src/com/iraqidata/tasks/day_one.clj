(ns com.iraqidata.tasks.day-one)


;; 1. Write a function that takes one argument as input and prints that
;; argument.
(defn input-print [x]
  (print x))

(input-print "Hello Clojure!")

;; 2. Write a function that adds `1` to a number only if the input is odd,
;; otherwise return `:error`.

;; Tip, use the `odd?` function to check if a number is odd.

(defn only-odds [x]
  (if (odd? x)
    (inc x)
    :error))

(only-odds 3)
(only-odds 2)

;; 3. Write a function that takes 3 arguments, `name`, `year-of-birth`, and
;; `current-year`. and returns a map with the following keys: `name`, `age`.

;; Example run
;; (function-name "Ali" 2001 2024) => {:name "Ali", :age 23}

(defn personal-info [name year-of-birth current-year]
  {:name name :age (- current-year year-of-birth)})
(personal-info "Ali" 2001 2024)


;; 4. Write a function that takes the output of the above function and returns
;; `true` if the person is allowed to vote (assume the voting age is 18).

;; Example run
;; (function-name {:name "Ali", :age 23}) => true
;; (function-name "Ali" 2001 2024) => true
;; (function-name {:name "Abbas", :age 17}) => false

(defn can-vote? [person-info]
  (>= (:age person-info) 18))
(def person (personal-info "Ali" 2001 2024))
(can-vote? person)

;; OPTIONAL FOR BONUS POINTS

;; 5. Modify the function from number 3 to not need the `current-year`.
;; Example run
;; (function-name "Ali" 2001) => {:name "Ali", :age 23}
;; If ran in 2025
;; (function-name "Ali" 2001) => {:name "Ali", :age 24}
(defn Current-Year []
  (.getYear (java.time.LocalDate/now)))

(defn personal-info-2 [name year-of-birth]
  {:name name :age (- (Current-Year) year-of-birth)})

(def person-2 (personal-info-2 "Ali" 2021))
(can-vote? person-2)