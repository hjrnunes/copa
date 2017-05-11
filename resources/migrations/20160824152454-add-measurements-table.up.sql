CREATE TABLE measurements
(measurement_id BIGINT NOT NULL AUTO_INCREMENT,
 quantity DOUBLE,
 unit VARCHAR(30),
 ingredient VARCHAR(50),
 CONSTRAINT pk_measurement_id PRIMARY KEY (measurement_id),
 CONSTRAINT fk_ingredient FOREIGN KEY (ingredient) REFERENCES ingredients(name));
