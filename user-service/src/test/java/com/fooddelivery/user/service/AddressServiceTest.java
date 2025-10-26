package com.fooddelivery.user.service;

import com.fooddelivery.common.security.SecurityUtils;
import com.fooddelivery.user.dto.AddressRequestDTO;
import com.fooddelivery.user.dto.AddressResponseDTO;
import com.fooddelivery.user.entity.User;
import com.fooddelivery.user.entity.UserAddress;
import com.fooddelivery.user.exception.AddressNotFoundException;
import com.fooddelivery.user.exception.UserNotFoundException;
import com.fooddelivery.user.mapper.AddressMapper;
import com.fooddelivery.user.repository.AddressRepository;
import com.fooddelivery.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressService addressService;

    private User testUser;
    private UserAddress testAddress;
    private AddressRequestDTO addressRequest;
    private AddressResponseDTO addressResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testAddress = new UserAddress();
        testAddress.setId(1L);
        testAddress.setUser(testUser);
        testAddress.setAddressLine("123 Main St");
        testAddress.setCity("New York");
        testAddress.setPostalCode("10001");
        testAddress.setCountry("USA");
        testAddress.setLatitude(new BigDecimal("40.7128"));
        testAddress.setLongitude(new BigDecimal("-74.0060"));
        testAddress.setIsDefault(true);
        testAddress.setLabel("Home");

        addressRequest = new AddressRequestDTO(
            "123 Main St",
            "New York",
            "10001",
            "USA",
            new BigDecimal("40.7128"),
            new BigDecimal("-74.0060"),
            true,
            "Home"
        );

        addressResponse = new AddressResponseDTO(
            1L,
            "123 Main St",
            "New York",
            "10001",
            "USA",
            new BigDecimal("40.7128"),
            new BigDecimal("-74.0060"),
            true,
            "Home"
        );
    }

    @Test
    void addAddress_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(addressMapper.toEntity(any(AddressRequestDTO.class))).thenReturn(testAddress);
            when(addressRepository.save(any(UserAddress.class))).thenReturn(testAddress);
            when(addressMapper.toResponse(any(UserAddress.class))).thenReturn(addressResponse);

            AddressResponseDTO result = addressService.addAddress(addressRequest);

            assertNotNull(result);
            assertEquals("123 Main St", result.addressLine());
            verify(addressRepository).save(any(UserAddress.class));
        }
    }

    @Test
    void addAddress_UserNotFound() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(999L);
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> addressService.addAddress(addressRequest));
        }
    }

    @Test
    void getMyAddresses_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            when(addressRepository.findByUserId(1L)).thenReturn(List.of(testAddress));
            when(addressMapper.toResponse(any(UserAddress.class))).thenReturn(addressResponse);

            List<AddressResponseDTO> result = addressService.getMyAddresses();

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(addressRepository).findByUserId(1L);
        }
    }

    @Test
    void getAddressById_Success() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));
        when(addressMapper.toResponse(testAddress)).thenReturn(addressResponse);

        AddressResponseDTO result = addressService.getAddressById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void getAddressById_NotFound() {
        when(addressRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AddressNotFoundException.class, () -> addressService.getAddressById(999L));
    }

    @Test
    void updateAddress_Success() {
        AddressRequestDTO updateRequest = new AddressRequestDTO(
            "456 Oak Ave",
            "Boston",
            "02101",
            "USA",
            new BigDecimal("42.3601"),
            new BigDecimal("-71.0589"),
            false,
            "Work"
        );

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(UserAddress.class))).thenReturn(testAddress);
            when(addressMapper.toResponse(any(UserAddress.class))).thenReturn(addressResponse);

            AddressResponseDTO result = addressService.updateAddress(1L, updateRequest);

            assertNotNull(result);
            verify(addressRepository).save(testAddress);
        }
    }

    @Test
    void deleteAddress_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));

            addressService.deleteAddress(1L);

            verify(addressRepository).delete(testAddress);
        }
    }

    @Test
    void setDefaultAddress_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));
            when(addressRepository.findByUserIdAndIsDefaultTrue(1L)).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(UserAddress.class))).thenReturn(testAddress);
            when(addressMapper.toResponse(any(UserAddress.class))).thenReturn(addressResponse);

            AddressResponseDTO result = addressService.setDefaultAddress(1L);

            assertNotNull(result);
            verify(addressRepository, times(2)).save(any(UserAddress.class));
        }
    }
}

