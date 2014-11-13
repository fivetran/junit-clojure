(ns com.fivetran.junit.example-spec
  (:require [com.fivetran.junit.reporter :as reporter])
  (:use clojure.test))

(use-fixtures :once reporter/with-junit)

(deftest succeeds
  (testing "one is one"
    (is (= 1 1))))

(deftest fails
  (testing "a is b"
    (is (= \a \b))))
