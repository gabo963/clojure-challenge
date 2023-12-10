(ns testing )
(load-file "src/invoice_item.clj")
(use 'clojure.test)

; cases

(def scenario1 {:invoice-item/precise-quantity 10 :invoice-item/precise-price 5 :invoice-item/discount-rate 50})
(def scenario2 {:invoice-item/precise-quantity 4 :invoice-item/precise-price 98 })

(def scenario3 {:invoice-item/precise-quantity 10 :invoice-item/precise-price 5 :invoice-item/discount-rate 110})

(def scenario4 {:invoice-item/precise-quantity 10 :invoice-item/precise-price 5 :invoice-item/discount-rate -34})

(def scenario5 {:invoice-item/precise-quantity 100 :invoice-item/precise-price -6 :invoice-item/discount-rate 20})
(def scenario6 {:invoice-item/precise-quantity 100 :invoice-item/precise-price -6 })

(def scenario7 {:invoice-item/precise-quantity -20 :invoice-item/precise-price 10 :invoice-item/discount-rate 20})
(def scenario8 {:invoice-item/precise-quantity -20 :invoice-item/precise-price 10})

(def scenario9 {:invoice-item/precise-quantity -4 :invoice-item/precise-price -12 :invoice-item/discount-rate -3})

; Basic Scenarios
(deftest basicCases
  (is (= (invoice-item/subtotal scenario1) 25.0))
  (is (= (invoice-item/subtotal scenario2) 392.0)))

(deftest overDiscounted
  (is (= (invoice-item/subtotal scenario3) 0.0 )))

(deftest negativeDiscount
  (is (= (invoice-item/subtotal scenario4) 0.0 )))

(deftest negativePrice
  (is (= (invoice-item/subtotal scenario5) 0.0 ))
  (is (= (invoice-item/subtotal scenario6) 0.0 )))

(deftest negativeQuantity
  (is (= (invoice-item/subtotal scenario7) 0.0 ))
  (is (= (invoice-item/subtotal scenario8) 0.0 )))

(deftest negativeValues
  (is (= (invoice-item/subtotal scenario9) 0.0 )))

(deftest arithmetic
  (basicCases)
  (overDiscounted)
  (negativeDiscount)
  (negativePrice)
  (negativeQuantity)
  (negativeValues))