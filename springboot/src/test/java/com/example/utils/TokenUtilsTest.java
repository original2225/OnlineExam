package com.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenUtilsTest {

    @Test
    void signsTokenWithConfiguredServerSecret() {
        TokenUtils tokenUtils = new TokenUtils();
        ReflectionTestUtils.setField(tokenUtils, "jwtSecret", "test-server-secret");
        tokenUtils.init();

        String token = TokenUtils.createToken("7-USER");
        String audience = JWT.require(Algorithm.HMAC256(TokenUtils.getJwtSecret()))
                .build()
                .verify(token)
                .getAudience()
                .get(0);

        assertEquals("7-USER", audience);
        assertThrows(Exception.class, () -> JWT.require(Algorithm.HMAC256("user-password"))
                .build()
                .verify(token));
    }
}
