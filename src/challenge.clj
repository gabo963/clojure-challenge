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
  (clojure.string/replace-first key #":\w+\/(\w+)" "$1")
  )

(defn worksMaps [value]
    [(first value) (update-keys (second value) #( keyword (str (renamer (first value) ) "/" (name %)) ))]
  )

(defn worksVec [value]
  (conj [(first value)] (into [] (map (fn [valor] (update-keys valor #(keyword (str (renamer (first value)) "/" (name %))))) (second value))))
  )
(defn invoiceCreator [filename]
  (update-keys
    (get (json/read-str (slurp "invoice.json") :key-fn keyword ) :invoice)
    #( keyword (str "invoice/" (name %)) ))
  )

(defn produceJsonInvoice [mapa]
  (into {} (map (fn [value]
                  (if (map? (second value))
                    (worksMaps value)
                    (if (vector? (second value))
                      (worksVec value)
                      value))) mapa))
  )

(defn run [filename]
  (produceJsonInvoice (invoiceCreator filename)))



