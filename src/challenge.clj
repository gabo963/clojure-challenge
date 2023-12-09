(ns challenge
  (:require [clojure.data.json :as json])
  )

;; PROBLEM 1
(def edn-invoice (clojure.edn/read-string (slurp "invoice.edn")))

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
  (->> invoice
       (:invoice/items)
       (reduce (fn [items item]
                 (if (xor
                       (check-tax (:taxable/taxes item) :iva 19 [:tax/category :tax/rate])
                       (check-tax (:retentionable/retentions item) :ret_fuente 1 [:retention/category :retention/rate])
                     )
                   (conj items item)
                   items))
               [])
       ))

;; PROBLEM 2

(defn invoiceCreator [filename]
  (json/read-str (slurp filename) :key-fn keyword))

(def invoice )