(ns tetris-clj.field)


(defn create-field [x y]
  (vec (repeat y (vec (repeat x 0)))))

(defn bake [field coords]
  (reduce (fn [f [x y]]
            (assoc-in f [y x] 1))
          field
          coords))

(defn full-row? [row]
  (not-any? zero? row))

(defn clear-full-rows [field]
  (let [width         (count (first field))
        non-full-rows (vec (remove full-row? field))
        cleared-count (- (count field) (count non-full-rows))
        empty-row     (vec (repeat width 0))]
    (vec (concat (repeat cleared-count empty-row) non-full-rows))))


