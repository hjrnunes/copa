-- :name get-measurement :? :1
-- :doc retrieve a measurement given the id.
SELECT * FROM measurements
WHERE measurement_id = :measurement_id

-- :name get-measurements-for-ids :? :*
-- :doc retrieve a measurement given a list of ids.
SELECT * FROM measurements
WHERE measurement_id IN (:v*:measurement_ids)

-- :name get-measurements :? :*
-- :doc retrieve all measurements
SELECT * FROM measurements

-- :name delete-measurement! :! :n
-- :doc delete a measurement given the id
DELETE FROM measurements
WHERE measurement_id = :measurement_id

-- :name create-measurement! :insert
-- :doc creates a new measurement record
/* :require [clojure.string :refer [join]] */
INSERT INTO measurements
--~ (str "(" (join ", " (map name (sort (keys params)))) ")")
-- ex (quantity, unit, ingredient)
VALUES
--~ (str "(" (join ", " (keys params)) ")")
-- ex (:quantity, :unit, :ingredient)

-- :name clean-measurements-table :! :n
-- :doc delete everything for testing
DELETE * FROM measurements