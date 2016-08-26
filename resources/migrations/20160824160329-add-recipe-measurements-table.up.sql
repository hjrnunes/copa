CREATE TABLE recipe_measurements
(rec_measurement_id BIGINT NOT NULL AUTO_INCREMENT,
 recipe_id VARCHAR(20),
 measurement_id VARCHAR(20),
 CONSTRAINT pk_recipe_measurement_id PRIMARY KEY (rec_measurement_id),
 CONSTRAINT fk_recipe_id FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id),
 CONSTRAINT fk_measurement_id FOREIGN KEY (measurement_id) REFERENCES measurements(measurement_id));
