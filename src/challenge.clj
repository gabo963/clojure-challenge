(ns challenge)

(defn invoice-filter-Last
  [invoice]
  (->> invoice
       (:invoice/items)
       ))

(defn check-tax
  [tax-set rate percentage]
  (filter #(and (= rate (:tax/category %)) (= percentage (:tax/rate %)) ) tax-set)
  )

(defn xor
  [cond1 cond2]
  (if (and cond1 cond2)
    false
    (or cond1 cond2)
    ))

(defn invoice-filter
  [invoice]
  (reduce (fn [item]
            (if (xor (check-tax (:taxable/taxes items) 19 :iva) )  ))
          []
          (:invoice/items invoice)
          ))