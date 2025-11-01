package com.cardsgame.CardsGame.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    /*
      Chiave segreta usata per firmare i token JWT.
      Nota: il codice usa Decoders.BASE64.decode(SECRET_KEY), quindi la stringa deve essere in Base64.
      Se la chiave è in formato esadecimale o altro, la decodifica dovrà essere adattata.
     */
    private static final String SECRET_KEY = "f6052089d4d088770a507c2c889917ef51de88f93237ddc66b5a858f805a2cbb";

    /*
      Estrae l'email (subject) dal token JWT.
      Internamente delega a extractClaim con Claims::getSubject.
      subject contenuto nel token (di solito l'email o username)
     */
    public String extractUserEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /*
      Estrae una singola rivendicazione (claim) dal token usando un resolver.
      - Legge tutti i claims con extractAllClaims
      - Applica la funzione claimsResolver per ottenere il valore desiderato
      claimsResolver funzione che estrae un campo specifico da Claims
      <T> tipo del valore estratto
      restituisce il valore estratto dalla claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /*
      Genera un token JWT senza claims aggiuntivi.
      Wrapper che chiama l'altro metodo passando una mappa vuota.
      userDetails dettagli dell'utente (username usato come subject)
      restituisce un token JWT firmato
     */
    public String genToken(UserDetails userDetails){
        return genToken(new HashMap<>(), userDetails);
    }

    /*
      Genera un token JWT con claims addizionali specificati.
      - setClaims aggiunge claims personalizzati
      - setSubject imposta il subject (username)
      - setIssuedAt e setExpiration impostano validità temporale
      - signWith firma il token con la chiave HMAC-SHA usando HS256
      extraClaims claims addizionali da includere
      userDetails dettagli dell'utente
      restituisce un token JWT compatto come stringa
     */
    public String genToken(Map<String, Object> extraClaims, UserDetails userDetails){
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // expiration calcolata aggiungendo millisecondi;
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /*
     Verifica se un token è valido rispetto ai dettagli dell'utente forniti.
     Controlla che il subject del token corrisponda allo username
     Controlla che il token non sia scaduto
    */
    public boolean isTokenValid(String token, UserDetails userDetails){
        final String userEmail = extractUserEmail(token);
        return (userEmail.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /*
     Controlla se il token è scaduto confrontando la sua expiration con la data corrente.
     */
    public boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    /*
     Estrae la data di scadenza (expiration) dal token.
     */
    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    /*
      Estrae tutte le claims presenti nel token dopo aver verificato la firma.
      Usa il parser di JJWT configurato con la chiave di firma.
      token token JWT
      restituisce i Claims presenti nel corpo del token
      lancia l'eccezione io.jsonwebtoken.JwtException se la firma non è valida o il token è scritto male
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /*
      Costruisce la chiave di firma a partire dalla stringa SECRET_KEY.
      - Decodifica la stringa con Decoders.BASE64.decode
      - Crea una Key HMAC-SHA utilizzabile da JJWT
      Attenzione: se la `SECRET_KEY` non è realmente in Base64, la decodifica fallirà.
      In molti esempi la chiave è fornita in Base64; se si possiede una chiave hex,
      bisogna usare una decodifica differente (es. DatatypeConverter.parseHexBinary).
      restituisce un oggetto Key per firmare o verificare JWT
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
