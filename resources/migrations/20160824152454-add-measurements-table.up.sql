CREATE TABLE measurements
(id BIGINT NOT NULL AUTO_INCREMENT,
 quantity DOUBLE,
 unit VARCHAR(30),
 ingredient VARCHAR(20),
 CONSTRAINT pk_measurement_id PRIMARY KEY (id),
 CONSTRAINT fk_ingredient FOREIGN KEY (ingredient) REFERENCES ingredients(id));
