(ns copa.macros)

(defmacro handler-fn
  ([& body]
   `(fn [~'event] ~@body nil)))                             ;; force return nil