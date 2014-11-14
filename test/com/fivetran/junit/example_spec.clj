(ns com.fivetran.junit.example-spec
  (:require [com.fivetran.junit.reporter :as reporter])
  (:use clojure.test))

(def ^:dynamic once-nil nil)

(def ^:dynamic each-nil nil)

(defn set-once [tests]
  (binding [once-nil "something"]
    (tests)))

(defn set-each [tests]
  (binding [each-nil "something"]
    (tests)))

(use-fixtures :once set-once)

(use-fixtures :each set-each)

(deftest dont-clear-locals
  (testing "should not be cleared in debugger"
    (let [a (list 1)
          b (list 2)
          c (list 3)
          d (list a b c)
          e (list 1)
          f (list 2)
          g (list 3)]
      (is d)
      (is e)
      (is f)
      (is g))))

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

(deftest once-fixture
  (testing "should run set-once"
    (is (= "something" once-nil))))

(deftest each-fixture
  (testing "should run set-each"
    (is (= "something" each-nil))))

(def non-test-var "This var doesn't have :test metadata")