(ns com.fivetran.junit.example-spec
  (:require [com.fivetran.junit.reporter :as reporter])
  (:use clojure.test))

(deftest succeeds
  (testing "one is one"
    (is (= 1 1))))

(deftest fails
  (testing "a is b"
    (is (= \a \b))))

(deftest expected-error
  (testing "throw Exception"
    (is (thrown? Exception (throw (Exception. "Expected"))))))

(deftest unexpected-error
  (testing "throw Exception"
    (throw (Exception. "I'm an uncaught exception in a test!"))))

(def non-test-var "This var doesn't have :test metadata")