(ns lein-check-missing-requires.t-core
  (:require [lein-check-missing-requires.core :as core]
            [clojure.test :refer :all])
  (:import (clojure.lang ExceptionInfo)))

(deftest source-file->ns-form
  (testing "it reads a file"
    (is (= (core/source-file->ns-form "test/lein_check_missing_requires/t_example.clj")
           '(ns lein-check-missing-requires.t-example)))))

(deftest ns-form->name
  (testing "it extracts it from a simple ns form"
    (is (= (core/ns-form->name '(ns my.awesome-ns)) 'my.awesome_ns))))

(deftest check-missing-requires-in-ns-form
  (testing "it passes when the form does not contain :import"
    (is (core/check-missing-requires-in-ns-form #{} "myns.clj"
          '(ns myns (:require [what :as ever])))))

  (testing "it passes when the imports are defined and required"
    (is (core/check-missing-requires-in-ns-form #{'my.defined-ns} "myns.clj"
          '(ns myns
             (:require [my.defined-ns])
             (:import (my.defined_ns MyClass))))))

  (testing "it raises when the imports are defined but not required"
    (is (thrown? ExceptionInfo
                 (core/check-missing-requires-in-ns-form #{'my.defined_ns} "myns.clj"
                   '(ns myns
                      (:import (my.defined_ns MyClass))))))))
