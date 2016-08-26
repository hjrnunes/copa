-- :name get-recipe :? :1
-- :doc retrieve a recipe given the id.
SELECT * FROM recipes
WHERE recipe_id = :recipe_id

-- :name get-recipe-by-name :? :1
-- :doc retrieve a recipe given the name.
SELECT * FROM recipes
WHERE name = :name

-- :name get-recipes :? :*
-- :doc retrieve all recipes
SELECT * FROM recipes

-- :name delete-recipe! :! :n
-- :doc delete a recipe given the id
DELETE FROM recipes
WHERE recipe_id = :recipe_id

-- :name create-recipe! :! :n
-- :doc creates a new recipe record
INSERT INTO recipes
(name, description, portions, source, preparation, user)
VALUES (:name, :description, :portions, :source, :preparation, :user)

-- :name update-recipe! :! :n
-- :doc update an existing recipe record
UPDATE recipes
SET name = :name, description = :description, portions = :portions, source = :source, preparation = :preparation
WHERE recipe_id = recipe_id

