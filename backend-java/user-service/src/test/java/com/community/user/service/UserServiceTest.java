package com.community.user.service;

import com.community.user.entity.User;
import com.community.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testGetUserByOpenid_Found() {
        // Given
        String openid = "test_openid_123";
        User user = new User();
        user.setId(1L);
        user.setOpenid(openid);
        user.setNickname("Test User");

        when(userRepository.findByOpenid(openid)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.getUserByOpenid(openid);

        // Then
        assertTrue(result.isPresent());
        assertEquals(openid, result.get().getOpenid());
        verify(userRepository, times(1)).findByOpenid(openid);
    }

    @Test
    void testGetUserByOpenid_NotFound() {
        // Given
        String openid = "non_existent_openid";
        when(userRepository.findByOpenid(openid)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserByOpenid(openid);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByOpenid(openid);
    }
}
