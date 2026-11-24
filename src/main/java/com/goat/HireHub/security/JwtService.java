package com.goat.HireHub.security;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

//    private final SecretKey signingKey;
//    private final long expirationMs;
//
//    public JwtService(
//            @Value("${security.jwt.secret}") String secret,
//            @Value("${security.jwt.expiration-ms}") long expirationMs) {
//
//        this.signingKey = Keys.hmacShaKeyFor(
//                Decoders.BASE64.decode(secret)
//        );
//
//        this.expirationMs = expirationMs;
//    }
//
//    public String generateToken(String email) {
//        Date now = new Date();
//        Date expiration = new Date(now.getTime() + expirationMs);
//
//        return Jwts.builder()
//                .subject(email)
//                .issuedAt(now)
//                .expiration(expiration)
//                .signWith(signingKey)
//                .compact();
//    }
//
//    private Claims extractAllClaims(String token) {
//        return Jwts.parser()
//                .verifyWith(signingKey)
//                .build()
//                .parseSignedClaims(token)
//                .getPayload();
//    }
//
//    public String extractEmail(String token){
//         return extractAllClaims(token).getSubject();
//    }
//
//    public boolean isTokenValid(String token, UserDetails userDetails) {
//        try {
//            Claims claims = extractAllClaims(token);
//
//            return claims.getSubject().equals(userDetails.getUsername())
//                    && claims.getExpiration().after(new Date());
//        } catch (JwtException | IllegalArgumentException e) {
//            return false;
//        }
//    }
//
//    public long getExpirationSeconds() {
//        return expirationMs / 1000;
//    }

    private static final String TOKEN_TYPE="token";
    private static final String ACCESS="access";
    private final SecretKey signingKey;
    private final long expirationMs;
    private final String issuer;
    private final JwtParser parser;

    public JwtService(
             @Value("${security.jwt.secret}")String secret,
             @Value("${security.jwt.expiration-ms}")long expirationMs,
             @Value("security.jwt.issuer:hirehub")String issuer
    ){
         byte[] keyBytes=Decoders.BASE64.decode(secret);
         if(keyBytes.length<32){
             throw new IllegalStateException("security.jwt.secret must be at least 32 bytes(256 bits)");
         }

         this.signingKey=Keys.hmacShaKeyFor(keyBytes);
         this.expirationMs=expirationMs;
         this.issuer=issuer;
         this.parser=Jwts.parser()
                 .verifyWith(signingKey)
                 .requireIssuer(issuer)
                 .require(TOKEN_TYPE,ACCESS)
                 .clockSkewSeconds(30)
                 .build();
    }


    public String generateToken(String email){
          Date now=new Date();
          return Jwts.builder()
                  .issuer(issuer)
                  .subject(email)
                  .claim(TOKEN_TYPE,ACCESS)
                  .id(UUID.randomUUID().toString())
                  .issuedAt(now)
                  .expiration(new Date(now.getTime()+expirationMs))
                  .signWith(signingKey,Jwts.SIG.HS256)
                  .compact();

    }

    private Claims parseClaims(String token){
        return parser.parseSignedClaims(token).getPayload();
    }

    public String extractEmail(String token){
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token,UserDetails user){
          try{
              String subject=parseClaims(token).getSubject();
              return subject!=null && subject.equals(user.getUsername());
          }catch(JwtException | IllegalArgumentException e){
               return false;
          }
    }

    public long getExpirationSeconds(){
          return expirationMs/1000;
    }
}
