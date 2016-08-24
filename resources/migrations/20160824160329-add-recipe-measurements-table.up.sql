CREATE TABLE recipe_measurements
(id BIGINT NOT NULL AUTO_INCREMENT,
 recipe_id VARCHAR(20),
 measurement_id VARCHAR(20),
 CONSTRAINT pk_recipe_measurement_id PRIMARY KEY (id),
 CONSTRAINT fk_recipe_id FOREIGN KEY (recipe_id) REFERENCES recipes(id),
 CONSTRAINT fk_measurement_id FOREIGN KEY (measurement_id) REFERENCES measurements(id));
