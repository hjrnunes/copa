CREATE TABLE recipes
(recipe_id BIGINT NOT NULL AUTO_INCREMENT,
 name VARCHAR(300) NOT NULL UNIQUE,
 description VARCHAR(300),
 portions VARCHAR(10),
 source VARCHAR(300),
 preparation VARCHAR NOT NULL,
 user VARCHAR(50),
 CONSTRAINT pk_recipe_id PRIMARY KEY (recipe_id),
 CONSTRAINT fk_user FOREIGN KEY (user) REFERENCES users(username));
