-- :name get-recipe-measurement :? :1
-- :doc retrieve a recipe-measurement given the id.
SELECT * FROM recipe_measurements
WHERE rec_measurement_id = :rec_measurement_id

-- :name get-recipe-measurements-for-recipe :? :*
-- :doc retrieve a recipes recipe-measurement given the recipe id.
SELECT * FROM recipe_measurements
WHERE recipe_id = :recipe_id

-- :name get-recipe-measurements :? :*
-- :doc retrieve all recipe measurements
SELECT * FROM recipe_measurements

-- :name delete-recipe-measurement! :! :n
-- :doc delete a measurement given the id
DELETE FROM recipe_measurements
WHERE rec_measurement_id = :rec_measurement_id

-- :name delete-recipe-measurements! :! :n
-- :doc delete all measurements for recipe id
DELETE FROM recipe_measurements
WHERE recipe_id = :recipe_id

-- :name create-recipe-measurement! :insert
-- :doc creates a new recipe measurement record
INSERT INTO recipe_measurements
(recipe_id, measurement_id)
VALUES (:recipe_id, :measurement_id)

