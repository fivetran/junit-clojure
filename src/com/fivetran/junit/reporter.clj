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
  [v each]
  (when-let [{t :test
              n :name} (meta v)]
    (binding [test/*testing-vars* (conj test/*testing-vars* v)]
      (proxy [TestCase] [(name n)]
        (runTest []
          (each
            t))))))

(defn test-vars
  "Wraps each var in a TestCase"
  {:added "1.6"}
  [vars]
  (for [[ns vars] (group-by (comp :ns meta) vars)]
    (let [each-fixture-fn (test/join-fixtures (:clojure.test/each-fixtures (meta ns)))]
      (for [v vars]
        (when (:test (meta v))
          (test-var v each-fixture-fn))))))

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
  (binding [*compiler-options* (assoc *compiler-options* :disable-locals-clearing true)]
    (let [ns-obj (the-ns ns)
          once-fixture-fn (test/join-fixtures (:clojure.test/once-fixtures (meta ns-obj)))
          grouped (test-all-vars ns-obj)
          tests (flatten grouped)
          somes (filter some? tests)
          suite (proxy [TestSuite] [(name ns)]
                  (run [test-result]
                    (binding [original-report test/report]
                      (binding [test/report junit-report]
                        (once-fixture-fn
                          (fn []
                            (proxy-super run test-result)))))))]
      (doseq [case somes]
        (.addTest suite case))

      suite)))

(defn require-debug [ns]
  (binding [*compiler-options* (assoc *compiler-options* :disable-locals-clearing true)]
    (require ns)))

(defn assert-equals
  "Returns generic assertion code for any functional predicate.  The
  'expected' argument to 'report' will contains the original form, the
  'actual' argument will contain the form with all its sub-forms
  evaluated.  If the predicate returns false, the 'actual' form will
  be wrapped in (not...)."
  {:added "1.1"}
  [msg form]
  (let [args (rest form)
        pred (first form)]
    `(let [values# (list ~@args)
           result# (apply ~pred values#)
           expected# (first values#)
           actual# (second values#)]
       (if result#
         (test/do-report {:type :pass, :message ~msg,
                          :expected expected#, :actual actual#})
         (test/do-report {:type :fail, :message ~msg,
                          :expected expected#, :actual actual#}))
       result#)))

(defmethod test/assert-expr '= [msg form]
  (let [args (rest form)]
    (if (= 2 (count args))
      (assert-equals msg form)
      (test/assert-predicate msg form))))