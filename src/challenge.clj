(ns challenge)

(def invoice (clojure.edn/read-string (slurp "invoice.edn")))

(defn invoice-filter-Last
  [invoice]
  (->> invoice
       (:invoice/items)
       ))

(defn check-tax
  [tax-set category percentage keys]
  (not-empty (filter #(and (= category ((first keys) %)) (= percentage ((second keys) %)) ) tax-set)) )

(defn xor
  [cond1 cond2]
  (if (and cond1 cond2)
    false
    (or cond1 cond2)
    ))

(defn invoice-filter
  [invoice]
  (reduce (fn [items item]
            (if (xor (check-tax (:taxable/taxes item) :iva 19 [:tax/category :tax/rate]) (check-tax (:retentionable/retentions item) :ret_fuente 1 [:retention/category :retention/rate]) )
              (conj items item)
              items))
          []
          (:invoice/items invoice)
          ))