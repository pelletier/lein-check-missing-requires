(ns lein-check-missing-requires.core
  (:require [leiningen.core.main :as lein]
            [clojure.set :as set]
            [clojure.string :as str])
  (:import (clojure.lang ExceptionInfo)))

(defn normalize-ns-name-symbol [name-symbol]
  (-> name-symbol
      (str)
      (str/replace "-" "_")
      (symbol)))

(defn and-fns [& fns]
  (fn [x] (every? true? ((apply juxt fns) x))))

(defn get-in-ns-form [sym ns-form]
  (first (filter (and-fns coll? #(= sym (first %))) (drop 2 ns-form))))

(defn ns-referenced-under-symbol [symbol ns-form]
  (->> ns-form
       (get-in-ns-form symbol)
       (rest)
       (map first)
       (set)))

(defn check-missing-requires-in-ns-form [known-ns path ns-form]
  (let [imports (ns-referenced-under-symbol :import ns-form)
        requires (->> (ns-referenced-under-symbol :require ns-form) (map normalize-ns-name-symbol) (set))
        missing (set/difference (set/intersection imports known-ns) requires)]
    (if-not (empty? missing)
      (throw (ex-info "Some imports are not required" {:file path :missing missing})))
    :success))

(defn pretty-check-missing-requires-in-ns-form [known-ns path ns-form]
  (try
    (check-missing-requires-in-ns-form known-ns path ns-form)
    (catch ExceptionInfo ex
      (let [data (ex-data ex)]
        (lein/warn (str (:file data) ": " (.getMessage ex) ": " (:missing data)))))))

(defn clj-file? [path]
  (.endsWith path ".clj"))

(defn list-clj-files [root-path]
  (->> root-path
       (clojure.java.io/file)
       (file-seq)
       (map #(.getPath %))
       (filter clj-file?)))

(defn clj-files-in-paths [source-paths]
  (->> source-paths
       (map list-clj-files)
       (apply concat)))

(defn source-file->ns-form [file]
  (read-string (slurp file)))

(defn ns-form->name [ns-form]
  (normalize-ns-name-symbol (second ns-form)))

(defn map-with-key [f coll]
  (->> coll
       (map (juxt identity f))
       (into {})))

(defn check-missing-requires-in-paths [source-paths]
  (lein/info "Checking missing requires in paths" source-paths)
  (let [source-files (clj-files-in-paths source-paths)
        sources-ns-forms (map-with-key source-file->ns-form source-files)
        known-namespaces (set (map ns-form->name (vals sources-ns-forms)))]
       (->> sources-ns-forms
         (map (fn [[path ns-form]] (pretty-check-missing-requires-in-ns-form known-namespaces path ns-form)))
         (dorun))))
