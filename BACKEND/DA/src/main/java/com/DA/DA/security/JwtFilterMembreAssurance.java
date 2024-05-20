package com.DA.DA.security;

import com.DA.DA.service.EmployeeService;
import com.DA.DA.service.SocieteService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
@Component
public class JwtFilterMembreAssurance extends OncePerRequestFilter {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtServiceMembre jwtServiceMembre;



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null;
        String email = null;

        final String authorization = request.getHeader("Authorization");
        if(authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
            email = jwtServiceMembre.lireEmployeEmail(token);
            if(email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtServiceMembre.isTokenExpired(token)) {
                    // Le token est expiré, régénérez-le
                    token = jwtServiceMembre.generate(email).get("bearer");
                    // Envoyer le nouveau token dans la réponse
                    response.setHeader("Authorization", "Bearer " + token);
                } else {
                    // Déchiffrer toutes les revendications du token JWT
                    Map<String, Object> claims = jwtServiceMembre.getAllClaims(token).get("claims", Map.class);
                    // Déchiffrer chaque revendication
                    for (Map.Entry<String, Object> entry : claims.entrySet()) {
                        String decryptedValue = jwtServiceMembre.decryptSensitiveData(entry.getValue().toString());
                        entry.setValue(decryptedValue);
                        // Ajouter la revendication déchiffrée comme en-tête dans la réponse
                        response.addHeader("X-JWT-Claim-" + entry.getKey(), decryptedValue);
                    }

                    // Utiliser ces revendications déchiffrées dans votre logique d'authentification ou d'autorisation

                    // Charger les détails de l'utilisateur à partir du service approprié (comme vous l'avez fait)
                    UserDetails userDetails = employeeService.loadUserByUsername(email);

                    // Créer l'objet d'authentification avec les détails de l'utilisateur
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    // Définir l'objet d'authentification dans le contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
