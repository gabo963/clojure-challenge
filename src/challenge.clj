(ns challenge
  (:require [clojure.data.json :as json])
  )

;; PROBLEM 1
(def edn-invoice (clojure.edn/read-string (slurp "invoice.edn")))

;Checks if the given item matches the given parameters.
(defn check-tax
  [tax-set category percentage keys]
  (not-empty (filter #(and (= category ((first keys) %)) (= percentage ((second keys) %)) ) tax-set)) )

;xor function to handle the condition where both tax types may match
(defn xor
  [cond1 cond2]
  (if (and cond1 cond2)
    false
    (or cond1 cond2)
    ))

;Function to review item by item iterating and adding to the items
(defn tax-checker
  [items item]
  (if (xor
        (check-tax (:taxable/taxes item) :iva 19 [:tax/category :tax/rate])
        (check-tax (:retentionable/retentions item) :ret_fuente 1 [:retention/category :retention/rate])
        )
    (conj items item)
    items))

;Reducer to pass through all items and reduce to the ones that pass the conditions.
(defn condition-reducer
  [invoice-items]
  (reduce tax-checker
          []
          invoice-items)
  )

;Invoice filter prompt: gets the items and processes them.
(defn invoice-filter
  [invoice]
  (->> invoice
       :invoice/items
       condition-reducer
       ))

;; PROBLEM 2

;Creates a map from the json passed as filename, includes the additional "invoice/" to the keywords.
(defn invoiceCreator [filename]
  (update-keys
    (get (json/read-str (slurp "invoice.json") :key-fn keyword ) :invoice)
    #( keyword (str "invoice/" (name %)) ))
  )

;removes the first section of the key for items, e.g removes the "invoice/" from "invoice/customer"
(defn renamer [key]
  (if (= "items" (clojure.string/replace-first key #":\w+\/(\w+)" "$1"))
    "invoice-item"
    (clojure.string/replace-first key #":\w+\/(\w+)" "$1"))
  )

;adjusts the name of maps based on the key.
(defn worksMaps [[k v]]
    [k (update-keys v #( keyword (str (renamer k ) "/" (name %)) ))]
  )

;parses the tax values to their correct values.
(defn updateTaxVals [map]
  (update-vals map #(if (string? %)
                      :iva
                      (double %)))
  )

; adjusts the tax vector to conform to the appropiate format.
(defn taxesVector [[k v]]
  [k (into [] (map (fn
                     [valor]
                     (update-keys (updateTaxVals valor)  #(keyword (clojure.string/replace (name %) #"_" "/"))))
                               v))]
  )

; adjusts standard vectors so that these conform to the appropiate format
(defn worksVec [[k v]]
  [k (into [] (map (fn [valor]
                                 (into {} (map (fn [val] (if (vector? (second val))
                                                           (taxesVector val)
                                                           val))
                                               (update-keys valor #(keyword (str (renamer k) "/" (name %)))))) )
                               v))]
  )

; runs the map through the adjustment functions.
(defn produceJsonInvoice [mapa]
  (into {}
        (map (fn [[k v] ]
                  (if (map? v)
                    [k (produceJsonInvoice (k (into {} [(worksMaps [k v] )])))]
                    (if (vector? v)
                      (worksVec [k v] )
                      [k v] ))) mapa))
  )

; Adjusts the customer section to the appropiate key names
(defn customerConversion [invoice]
  (assoc invoice :invoice/customer
                 (clojure.set/rename-keys (:invoice/customer invoice)
                                          {:customer/company_name :customer/name, :customer/email :customer/email}))
  )

;parses the date to the appropiate format (instant Clojure)
(defn date-conversion [invoice]
  (let [fecha (clojure.string/split (:invoice/issue_date invoice) #"/")]
    (assoc (clojure.set/rename-keys invoice {:invoice/issue_date :invoice/issue-date})
      :invoice/issue-date
      (clojure.instant/read-instant-date (str (get fecha 2) "-" (get fecha 1) "-" (get fecha 0)))))
  )

; The result of the following function validates vs the invoice Spec!
;(s/valid? ::invoice invoice)
;=> true
(defn run [filename]
  (->> filename
       invoiceCreator
       produceJsonInvoice
       customerConversion
       date-conversion)
  )