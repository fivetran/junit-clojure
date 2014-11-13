(ns com.fivetran.junit.reporter
  (:require [clojure.test :as test]))

(def ^{:dynamic true
       :private true} original-report)

(defmulti junit-report
          "Intercept clojure test reports and forward them to JUnit"
          :type)

(defmethod junit-report :fail [m]
  (println m)

  (original-report m))

(defmethod junit-report :error [m]
  (println m)

  (original-report m))

(defmethod junit-report :default [m]
  (original-report m))

(defn test-junit
  "(test-junit ns) => [TestCase...]"
  [ns]
  (test/test-ns ns))

(defn with-junit [tests]
  (binding [original-report test/report]
    (binding [test/report junit-report]
      (tests))))

(defn test-var
  "If v has a function in its :test metadata, calls that function,
  with *testing-vars* bound to (conj *testing-vars* v)."
  {:dynamic true, :added "1.1"}
  [v]
  (when-let [t (:test (meta v))]
    (binding [test/*testing-vars* (conj test/*testing-vars* v)]
      (test/do-report {:type :begin-test-var, :var v})
      (test/inc-report-counter :test)
      (try (t)
           (catch Throwable e
             (test/do-report {:type :error, :message "Uncaught exception, not in assertion."
                         :expected nil, :actual e})))
      (test/do-report {:type :end-test-var, :var v}))))

(defn test-vars
  "Groups vars by their namespace and runs test-vars on them with
   appropriate fixtures applied."
  {:added "1.6"}
  [vars]
  (doseq [[ns vars] (group-by (comp :ns meta) vars)]
    (let [once-fixture-fn (test/join-fixtures (::once-fixtures (meta ns)))
          each-fixture-fn (test/join-fixtures (::each-fixtures (meta ns)))]
      ;; TODO before global?
      (once-fixture-fn
        (fn []
          (doseq [v vars]
            (when (:test (meta v))
              ;; TODO override setup(), tearDown() in TestCase
              (each-fixture-fn (fn [] (test-var v))))))))))

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
  (binding [test/*report-counters* (ref test/*initial-report-counters*)]
    (let [ns-obj (the-ns ns)]
      (test/do-report {:type :begin-test-ns, :ns ns-obj})
      (test-all-vars ns-obj) ; test-ns-hook is ignored
      (test/do-report {:type :end-test-ns, :ns ns-obj}))
    @test/*report-counters*))