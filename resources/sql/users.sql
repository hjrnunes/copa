-- :name get-user :? :1
-- :doc retrieve a user given the username.
SELECT * FROM users
WHERE username = :username

-- :name get-users :? :*
-- :doc retrieve all users
SELECT * FROM users

-- :name delete-user! :! :n
-- :doc delete a user given the username
DELETE FROM users
WHERE username = :username

-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(username, password, admin)
VALUES (:username, :password, :admin)

-- :name update-user-password! :! :n
-- :doc update an existing user password
UPDATE users
SET password = :password
WHERE username = :username

-- :name update-user-lang! :! :n
-- :doc update an existing user language
UPDATE users
SET lang = :lang
WHERE username = :username

-- :name clean-users-table :! :n
-- :doc delete everything for testing
DELETE * FROM users