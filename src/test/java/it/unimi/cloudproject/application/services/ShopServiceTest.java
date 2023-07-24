package it.unimi.cloudproject.application.services;

import it.unimi.cloudproject.application.dto.ShopCreation;
import it.unimi.cloudproject.factories.bl.ShopFactory;
import it.unimi.cloudproject.factories.bl.valueobjects.CoordinatesFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ShopServiceTest {
    @Autowired
    private ShopService shopService;

    @Test
    void givenAShop_whenSearchByItsName_thenReturnIt() {
        shopService.addShop(new ShopCreation(ShopFactory.VALID_SHOP_NAME,
                CoordinatesFactory.VALID_LONGITUDE,
                CoordinatesFactory.VALID_LATITUDE));

        var shops = shopService.findByName(ShopFactory.VALID_SHOP_NAME);

        assertThat(shops).hasSize(1);
    }
}
