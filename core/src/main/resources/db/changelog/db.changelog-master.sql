--liquibase formatted sql

--changeset author:test
CREATE TABLE "USER" (
    id SERIAL,
    username varchar(50) NOT NULL,

    CONSTRAINT pk_user_id PRIMARY KEY (id)
);

CREATE TABLE "SHOP" (
    id SERIAL,
    shop_owner int NOT NULL,
    name varchar(100) NOT NULL,
    latitude float NOT NULL,
    longitude float NOT NULL,

    CONSTRAINT pk_shop_id PRIMARY KEY (id),
    CONSTRAINT fk_shop_userOwner FOREIGN KEY (shop_owner) REFERENCES "USER"(id)
                       ON DELETE CASCADE ON UPDATE RESTRICT
);

CREATE TABLE "USER_SHOP" (
    user_id int,
    shop_id int,

    CONSTRAINT pk_user_id_shop_id PRIMARY KEY (user_id, shop_id),
    CONSTRAINT fk_user_shop_user_id FOREIGN KEY (user_id) REFERENCES "USER"(id)
        ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_user_shop_shop_id FOREIGN KEY (shop_id) REFERENCES "SHOP"(id)
        ON DELETE CASCADE ON UPDATE RESTRICT
);