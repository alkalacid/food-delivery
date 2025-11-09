package com.fooddelivery.order.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.services")
@Getter
@Setter
public class ExternalServicesProperties {
    
    private RestaurantService restaurant = new RestaurantService();
    private UserService user = new UserService();
    
    @Getter
    @Setter
    public static class RestaurantService {
        private String baseUrl = "http://restaurant-service";
        private String restaurantsEndpoint = "/api/restaurants";
        private String menuItemsBatchEndpoint = "/api/menu-items/batch";
    }
    
    @Getter
    @Setter
    public static class UserService {
        private String baseUrl = "http://user-service";
        private String addressesEndpoint = "/api/addresses";
    }
}

