-- :name get-recipe :? :1
-- :doc retrieve a recipe given the id.
SELECT * FROM recipes
WHERE recipe_id = :recipe_id

-- :name get-recipe-by-name :? :1
-- :doc retrieve a recipe given the name.
SELECT * FROM recipes
WHERE name = :name

-- :name get-recipes-of-user :? :1
-- :doc retrieve all a user's recipes.
SELECT * FROM recipes
WHERE user = :user

-- :name get-recipes :? :*
-- :doc retrieve all recipes
SELECT * FROM recipes

-- :name delete-recipe! :! :n
-- :doc delete a recipe given the id
DELETE FROM recipes
WHERE recipe_id = :recipe_id

-- :name create-recipe! :insert
-- :doc creates a new recipe record
/* :require [clojure.string :refer [join]] */
INSERT INTO recipes
--~ (str "(" (join ", " (map name (sort (keys params)))) ")")
VALUES
--~ (str "(" (join ", " (sort (keys params))) ")")

-- :name update-recipe! :! :n
-- :doc update an existing recipe record
/* :require [clojure.string :refer [join]] */
UPDATE recipes
SET
--~ (str (join ", " (map #(str (name %) " = " %) (keys params))))
WHERE recipe_id = :recipe_id

-- :name clean-recipes-table :! :n
-- :doc delete everything for testing
DELETE FROM recipes

-- :name get-recipes-for-ingredient-name :? :*
-- :doc get recipes which contain
SELECT recipes.* FROM recipes
JOIN recipe_measurements ON recipes.recipe_id = recipe_measurements.recipe_id
JOIN measurements ON recipe_measurements.measurement_id = measurements.measurement_id
JOIN ingredients ON measurements.ingredient = ingredients.name
WHERE ingredients.name IN (:v*:ingredients)

-- :name get-recipes-containing :? :*
-- :doc get ids of recipes that contain all the ingredients passed
SELECT recipe_id
FROM (
  SELECT recipe_id, ingredients.name FROM recipe_measurements
  JOIN measurements ON recipe_measurements.measurement_id = measurements.measurement_id
  JOIN ingredients ON measurements.ingredient = ingredients.name
)
WHERE name IN (:v*:ingredients)
GROUP BY recipe_id
--~ (str "HAVING COUNT(name) = " (count (:ingredients params)))