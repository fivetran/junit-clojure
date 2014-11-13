(ns com.fivetran.junit.reporter
  (:import (junit.framework TestCase TestSuite))
  (:require [clojure.test :as test]))

(def ^{:dynamic true
       :private true} original-report)

(defmulti junit-report
          "Intercept clojure test reports and forward them to JUnit"
          :type)

(defmethod junit-report :fail [{message :message
                                expected :expected
                                actual :actual
                                file :file
                                line :line}]
  (let [context (test/testing-contexts-str)
        location (str \< file \: line \>)
        alt-message (str context " " location)
        message (or message alt-message)]
    (TestCase/failNotEquals message expected actual)))

(defmethod junit-report :error [{actual :actual}]
  (throw actual))

(defmethod junit-report :default [m]
  (original-report m))

(defn test-junit
  "(test-junit ns) => [TestCase...]"
  [ns]
  (test/test-ns ns))

(defn test-var
  "If v has a function in its :test metadata, calls that function,
  with *testing-vars* bound to (conj *testing-vars* v)."
  {:dynamic true, :added "1.1"}
  [v]
  (when-let [{t :test
              n :name} (meta v)]
    (binding [test/*testing-vars* (conj test/*testing-vars* v)]
      (proxy [TestCase] [(name n)]
        (runTest []
          (t))))))

(defn test-vars
  "Wraps each var in a TestCase"
  {:added "1.6"}
  [vars]
  (for [[ns vars] (group-by (comp :ns meta) vars)]
    (let [once-fixture-fn (test/join-fixtures (::once-fixtures (meta ns)))
          each-fixture-fn (test/join-fixtures (::each-fixtures (meta ns)))]
      (for [v vars]
        (when (:test (meta v))
          (test-var v))))))

(defn test-all-vars
  "Calls test-vars on every var interned in the namespace, with fixtures."
  {:added "1.1"}
  [ns]
  (test-vars (vals (ns-interns ns))))

(defn test-ns
  "If the namespace defines a function named test-ns-hook, calls that.
  Otherwise, calls test-all-vars on the namespace.  'ns' is a
  namespace object or a symbol.

  Internally binds *report-counters* to a ref initialized to
  *initial-report-counters*.  Returns the final, dereferenced state of
  *report-counters*."
  {:added "1.1"}
  [ns]
  (let [ns-obj (the-ns ns)
        grouped (test-all-vars ns-obj)
        tests (flatten grouped)
        somes (filter some? tests)
        suite (proxy [TestSuite] [(name ns)]
                (run [test-result]
                  (binding [original-report test/report]
                    (binding [test/report junit-report]
                      (proxy-super run test-result)))))]
    (doseq [case somes]
      (.addTest suite case))

    suite))