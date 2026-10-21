package org.ltdz.taskflow.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与解析。
 * <p>
 * 密钥与有效期来自 application.yaml 的 taskflow.jwt.*。
 */
@Service
public class JwtService {

    @Value("${taskflow.jwt.secret}")
    private String secret;

    @Value("${taskflow.jwt.expire-hours:12}")
    private long expireHours;

    /** 签名密钥，启动时初始化一次 */
    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 签发 token。
     *
     * @param payload 至少需要 userId；expireAt 为空时按配置的有效期自动计算
     * @return JWT 字符串
     */
    public String createToken(JwtPayload payload) {
        Date now = new Date();
        Date issuedAt = payload.getIssuedAt() != null ? payload.getIssuedAt() : now;
        Date expireAt = payload.getExpireAt() != null
                ? payload.getExpireAt()
                : new Date(now.getTime() + expireHours * 60 * 60 * 1000L);

        return Jwts.builder()
                .subject(String.valueOf(payload.getUserId()))
                .issuedAt(issuedAt)
                .expiration(expireAt)
                .signWith(key)
                .compact();
    }

    /**
     * 解析并校验 token（签名 + 有效期）。
     * <p>
     * 校验失败会抛出 {@link io.jsonwebtoken.JwtException}；已过期时会抛
     * {@link io.jsonwebtoken.ExpiredJwtException}。
     *
     * @param token JWT 字符串
     * @return 解析后的载荷
     */
    public JwtPayload parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        JwtPayload payload = new JwtPayload();
        payload.setUserId(Long.valueOf(claims.getSubject()));
        payload.setIssuedAt(claims.getIssuedAt());
        payload.setExpireAt(claims.getExpiration());
        return payload;
    }
}
