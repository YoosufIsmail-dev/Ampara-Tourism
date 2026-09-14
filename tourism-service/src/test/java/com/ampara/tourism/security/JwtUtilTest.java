package com.ampara.tourism.security;

import com.ampara.tourism.entity.Role;
import com.ampara.tourism.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generatesTokenThatIsValidForTheSameUser() {
        User user = new User("Test Tourist", "tourist@example.com", "hashed", Role.TOURIST);

        String token = jwtUtil.generateToken(user, user.getRole().name());

        assertNotNull(token);
        assertEquals("tourist@example.com", jwtUtil.extractUsername(token));
        assertEquals("TOURIST", jwtUtil.extractRole(token));
        assertTrue(jwtUtil.isTokenValid(token, user));
    }

    @Test
    void tokenIsInvalidForADifferentUser() {
        User owner = new User("Owner", "owner@example.com", "hashed", Role.TOURIST);
        User other = new User("Other", "other@example.com", "hashed", Role.TOURIST);

        String token = jwtUtil.generateToken(owner, owner.getRole().name());

        assertFalse(jwtUtil.isTokenValid(token, other));
    }
}
