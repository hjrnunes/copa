-- :name get-ingredient :? :1
-- :doc retrieve an ingredient given the name.
SELECT * FROM ingredients
WHERE name = :name

-- :name get-ingredients :? :*
-- :doc retrieve all ingredients
SELECT * FROM ingredients

-- :name delete-ingredient! :! :n
-- :doc delete an ingredient given the username
DELETE FROM ingredients
WHERE name = :name

-- :name create-ingredient! :! :n
-- :doc creates a new ingredient record
INSERT INTO ingredients
(name)
VALUES (:name)

-- :name clean-ingredients-table :! :n
-- :doc delete everything for testing
DELETE * FROM ingredients