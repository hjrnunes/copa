CREATE TABLE ingredients
(ingredient_id BIGINT NOT NULL AUTO_INCREMENT,
 name VARCHAR(50) NOT NULL UNIQUE,
 CONSTRAINT pk_ingredient_id PRIMARY KEY (ingredient_id));
