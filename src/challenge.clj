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

(defn renamer [key]
  (if (= "items" (clojure.string/replace-first key #":\w+\/(\w+)" "$1"))
    "invoice-item"
    (clojure.string/replace-first key #":\w+\/(\w+)" "$1"))
  )

(defn worksMaps [value]
    [(first value) (update-keys (second value) #( keyword (str (renamer (first value) ) "/" (name %)) ))]
  )

(defn updateTaxVals [map]
  (update-vals map #(if (string? %)
                      :iva
                      (double %)))
  )

(defn worksVec-save [value]
  [(first value) (into [] (map (fn [valor]
                                 (update-keys (updateTaxVals valor)  #(keyword (clojure.string/replace (name %) #"_" "/"))))
                               (second value)))]
  )
(defn worksVec [value]
  [(first value) (into [] (map (fn [valor]
                                 (into {} (map (fn [val] (if (vector? (second val))
                                                           (worksVec-save val)
                                                           val))
                                               (update-keys valor #(keyword (str (renamer (first value)) "/" (name %)))))) )
                               (second value)))]
  )
(defn invoiceCreator [filename]
  (update-keys
    (get (json/read-str (slurp "invoice.json") :key-fn keyword ) :invoice)
    #( keyword (str "invoice/" (name %)) ))
  )

(defn produceJsonInvoice [mapa]
  (into {} (map (fn [value]
                  (if (map? (second value))
                    [(first value) (produceJsonInvoice ((first value) (into {} [(worksMaps value)])))]
                    (if (vector? (second value))
                      (worksVec value)
                      value))) mapa))
  )

(defn customerConversion [invoice]
  (assoc invoice :invoice/customer (clojure.set/rename-keys (:invoice/customer invoice) {:customer/company_name :customer/name, :customer/email :customer/email}))
  )

(defn dateConversion [invoice]
  (clojure.set/rename-keys invoice {:invoice/issue_date :invoice/issue-date})
  )

(defn run [filename]
  (->> filename
       invoiceCreator
       produceJsonInvoice
       customerConversion
       dateConversion)
  )
