package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.dto.RestaurantRequestDTO;
import com.fooddelivery.restaurant.dto.RestaurantResponseDTO;
import com.fooddelivery.restaurant.dto.RestaurantSearchDTO;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.exception.RestaurantNotFoundException;
import com.fooddelivery.restaurant.exception.UnauthorizedAccessException;
import com.fooddelivery.restaurant.mapper.RestaurantMapper;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantMapper restaurantMapper;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant testRestaurant;
    private RestaurantRequestDTO restaurantRequest;
    private RestaurantResponseDTO restaurantResponse;

    @BeforeEach
    void setUp() {
        testRestaurant = new Restaurant();
        testRestaurant.setId(1L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setDescription("Great food");
        testRestaurant.setOwnerId(1L);
        testRestaurant.setAddress("123 Food St");
        testRestaurant.setCity("New York");
        testRestaurant.setPostalCode("10001");
        testRestaurant.setLatitude(new BigDecimal("40.7128"));
        testRestaurant.setLongitude(new BigDecimal("-74.0060"));
        testRestaurant.setPhone("+1234567890");
        testRestaurant.setEmail("test@restaurant.com");
        testRestaurant.setCuisineType("Italian");
        testRestaurant.setAverageRating(BigDecimal.valueOf(4.5));
        testRestaurant.setTotalReviews(100);
        testRestaurant.setIsActive(true);
        testRestaurant.setOpeningTime(LocalTime.of(9, 0));
        testRestaurant.setClosingTime(LocalTime.of(22, 0));

        restaurantRequest = new RestaurantRequestDTO(
            "Test Restaurant",
            "Great food",
            "123 Food St",
            "New York",
            "10001",
            new BigDecimal("40.7128"),
            new BigDecimal("-74.0060"),
            "+1234567890",
            "test@restaurant.com",
            "Italian",
            LocalTime.of(9, 0),
            LocalTime.of(22, 0)
        );

        restaurantResponse = new RestaurantResponseDTO(
            1L,
            "Test Restaurant",
            "Great food",
            1L,
            "123 Food St",
            "New York",
            "10001",
            new BigDecimal("40.7128"),
            new BigDecimal("-74.0060"),
            "+1234567890",
            "test@restaurant.com",
            "Italian",
            BigDecimal.valueOf(4.5),
            100,
            true,
            LocalTime.of(9, 0),
            LocalTime.of(22, 0),
            List.of()
        );
    }

    @Test
    void getRestaurantById_Success() {
        when(restaurantRepository.findActiveById(1L)).thenReturn(Optional.of(testRestaurant));
        when(restaurantMapper.toResponse(testRestaurant)).thenReturn(restaurantResponse);

        RestaurantResponseDTO result = restaurantService.getRestaurantById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Test Restaurant", result.name());
        verify(restaurantRepository).findActiveById(1L);
    }

    @Test
    void getRestaurantById_NotFound() {
        when(restaurantRepository.findActiveById(999L)).thenReturn(Optional.empty());

        assertThrows(RestaurantNotFoundException.class, 
            () -> restaurantService.getRestaurantById(999L));
    }

    @Test
    void getAllRestaurants_Success() {
        when(restaurantRepository.findAllActive()).thenReturn(List.of(testRestaurant));
        when(restaurantMapper.toResponse(any(Restaurant.class))).thenReturn(restaurantResponse);

        List<RestaurantResponseDTO> result = restaurantService.getAllRestaurants();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(restaurantRepository).findAllActive();
    }

    @Test
    void createRestaurant_Success() {
        when(restaurantMapper.toEntity(any(RestaurantRequestDTO.class))).thenReturn(testRestaurant);
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(testRestaurant);
        when(restaurantMapper.toResponse(any(Restaurant.class))).thenReturn(restaurantResponse);

        RestaurantResponseDTO result = restaurantService.createRestaurant(restaurantRequest, 1L);

        assertNotNull(result);
        assertEquals("Test Restaurant", result.name());
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    void updateRestaurant_Success() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(testRestaurant);
        when(restaurantMapper.toResponse(any(Restaurant.class))).thenReturn(restaurantResponse);

        RestaurantResponseDTO result = restaurantService.updateRestaurant(1L, restaurantRequest, 1L);

        assertNotNull(result);
        verify(restaurantMapper).updateEntity(restaurantRequest, testRestaurant);
        verify(restaurantRepository).save(testRestaurant);
    }

    @Test
    void updateRestaurant_Unauthorized() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));

        assertThrows(UnauthorizedAccessException.class,
            () -> restaurantService.updateRestaurant(1L, restaurantRequest, 999L));
    }

    @Test
    void deleteRestaurant_Success() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));

        restaurantService.deleteRestaurant(1L, 1L);

        assertNotNull(testRestaurant.getDeletedAt());
        assertFalse(testRestaurant.getIsActive());
        verify(restaurantRepository).save(testRestaurant);
    }

    @Test
    void searchRestaurants_Success() {
        RestaurantSearchDTO searchDTO = new RestaurantSearchDTO(
            "Italian",
            "New York",
            new BigDecimal("40.7128"),
            new BigDecimal("-74.0060"),
            10.0,
            null
        );

        when(restaurantRepository.findNearbyRestaurants(
            any(BigDecimal.class), any(BigDecimal.class), anyDouble(), anyString()
        )).thenReturn(List.of(testRestaurant));
        when(restaurantMapper.toResponse(any(Restaurant.class))).thenReturn(restaurantResponse);

        List<RestaurantResponseDTO> result = restaurantService.searchRestaurants(searchDTO);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}

