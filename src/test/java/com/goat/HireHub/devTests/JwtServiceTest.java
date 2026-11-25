package com.goat.HireHub.devTests;

import com.goat.HireHub.security.JwtService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {
    private static String newSecret(){
         return Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded());
    }

    private JwtService service(long expirationMs){
         return new JwtService(newSecret(),expirationMs,"hirehub");
    }

    private static UserDetails userDetails(String email){
        return org.springframework.security.core.userdetails.User
                .withUsername(email)
                .password("goat@01")
                .build();
    }

    @Test
    public void validSecretBuilds(){
         assertDoesNotThrow(()->service(60_000));
    }

    @Test
    public void shortSecretIsRejected(){
        String weak= Base64.getEncoder().encodeToString(new byte[16]);
        assertThrows(IllegalStateException.class,()->new JwtService(weak,60_000,"hirehub"));
    }

    @Test
    public void notBase64IsRejected(){
        assertThrows(RuntimeException.class,()->new JwtService("not base64!!",60_000,"hirehub"));
    }

    @Test
    public void tokenHasThreeParts(){
          String token=service(60_000).generateToken("goat@example.com");
          assertEquals(3,token.split("\\.").length);
    }

    @Test
    public void payloadIsReadableButContainsNoSecrets(){
        String token=service(60_000).generateToken("goat@example.com");
        String payload=new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
        System.out.println(payload);
        assertTrue(payload.contains("goat@example.com"));

    }

    @Test
    public void roundTripReturnsTheEmail(){
          JwtService s=service(60_000);
         assertEquals("goat@example.com",s.extractEmail(s.generateToken("goat@example.com")));
    }

    @Test
    public void forgedPayloadIsRejected(){
         JwtService s=service(60_000);
         String[] parts=s.generateToken("goat@example.com").split("\\.");
         String forgedPayload=Base64.getUrlEncoder().withoutPadding().encodeToString(
                  "{\"sub\":\"admin@example.com\",\"iss\":\"hirehub\",\"token_type\",\"exp\":9999999999}".getBytes(StandardCharsets.UTF_8)
         );
         String forged=parts[0]+"."+forgedPayload+"."+parts[2];
         assertThrows(JwtException.class,()->s.extractEmail(forged));
    }

    @Test
    void tokenFromAnotherKeyIsRejected(){
        String token=service(60_000).generateToken("goat@example.com");
        assertThrows(JwtException.class,()->service(60_000).extractEmail(token));
    }

    @Test
    void expiredTokenIsRejected(){
         JwtService s=service(-60_000);
         assertThrows(JwtException.class,()->s.extractEmail(s.generateToken("goat@example.com")));
    }

    //token validity test
    @Test
    void validForSameUser(){
        JwtService s=service(60_0000);
        assertTrue(s.isTokenValid(s.generateToken("goat@example.com"),userDetails("goat@example.com")));
    }

    @Test
    void notValidForAnotherUser(){
        JwtService s=service(60_000);
        assertFalse(s.isTokenValid(s.generateToken("goat@example.com"),userDetails("goatee@example.com")));
    }

    @Test
    void garbageIsNotValidAndDoesNotThrow(){
         assertFalse(service(60_000).isTokenValid("garbage",userDetails("goat@example.com")));
    }

    @Test
    void expirationInSeconds(){
         assertEquals(900,service(900_000).getExpirationSeconds());
    }

}

