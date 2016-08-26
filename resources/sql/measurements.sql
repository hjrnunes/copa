-- :name get-measurement :? :1
-- :doc retrieve a measurement given the id.
SELECT * FROM measurements
WHERE measurement_id = :measurement_id

-- :name get-measurements :? :*
-- :doc retrieve all measurements
SELECT * FROM measurements

-- :name delete-measurement! :! :n
-- :doc delete a measurement given the id
DELETE FROM measurements
WHERE measurement_id = :measurement_id

-- :name create-measurement! :insert
-- :doc creates a new measurement record
INSERT INTO measurements
(quantity, unit, ingredient)
VALUES (:quantity, :unit, :ingredient)

