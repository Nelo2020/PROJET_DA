package com.DA.DA.security;

import com.DA.DA.entite.Role;
import com.DA.DA.entite.TypeRoles;
import com.DA.DA.service.EmployeeService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.DA.DA.entite.TypeRoles.ADMINISTRATEUR;
import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
public class ConfigurationSecuriteApplication {

    @Autowired
    private JwtFilter jwtFilter;
    private  final  BCryptPasswordEncoder passwordEncoder;
    public ConfigurationSecuriteApplication(JwtFilter jwtFilter, BCryptPasswordEncoder passwordEncoder) {
        this.jwtFilter = jwtFilter;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        return
                httpSecurity
                        .csrf(AbstractHttpConfigurer::disable)
                        .authorizeHttpRequests(
                                authorize ->
                                        authorize

                                                //Route gestion de connexion
                                                .requestMatchers(POST,"/api/CONNEXION_COMPTES/connexion**").permitAll()

                                                //Route retrouverInfo
                                                .requestMatchers(POST,"/api/RetrouverInfo/withEmail**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(GET,"/api/RetrouverInfo/withtoken**").authenticated()

                                                //Route retrouverInfo TABLE TEMPORAIRE
                                                .requestMatchers(GET,"/api/RetrouverInfo/rechercheParSocieteTemporaireVIE/{nom}/{emailMembre}/{dateSoumission}**").permitAll()
                                                .requestMatchers(GET,"/api/RetrouverInfo/rechercheParSocieteTemporaireIARD/{nom}/{emailMembre}/{dateSoumission}**").permitAll()
                                                .requestMatchers(GET,"/api/RetrouverInfo/rechercheParSocieteTemporaireCOURTIER/{nom}/{emailMembre}/{dateSoumission}**").permitAll()

                                                //Route retrouverInfo TABLE REELLE
                                                .requestMatchers(GET,"/api/RetrouverInfo/rechercheParSocieteReelleVIE/{nom}/{emailMembre}/{dateSoumission}**").permitAll()
                                                .requestMatchers(GET,"/api/RetrouverInfo/rechercheParSocieteReelleIARD/{nom}/{emailMembre}/{dateSoumission}**").permitAll()
                                                .requestMatchers(GET,"/api/RetrouverInfo/rechercheParSocieteReelleCOURTIER/{nom}/{emailMembre}/{dateSoumission}**").permitAll()




                                                //Route Modifications identifiants(motpasse par l'admin)

                                                .requestMatchers(PUT,"/api/MODIFICATION/modifier/Employe_DA**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(PUT,"/api/MODIFICATION/modifier/Membre**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(PUT,"/api/MODIFICATION/modifier/SOCIETE**").hasAuthority("ROLE_ADMINISTRATEUR")







                                                //gestion de users de la DA par l'admin
                                                // .requestMatchers(POST,"/api/DA/creer/**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(POST,"/api/DA/creer/**").permitAll()

                                                .requestMatchers(GET, "/api/DA/users/EMPLOYEE/**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(GET, "/api/DA/users/ADMINISTRATEUR/**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(GET, "/api/DA/users/**").hasAuthority("ROLE_ADMINISTRATEUR")


//



                                                //Route gestion des Societes par l'admin
                                                .requestMatchers(POST,"/api/Societe/creer/**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(GET,"/api/Societe/societe/VIE/**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(GET,"/api/Societe/societe/IARD/**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(GET,"/api/Societe/societe/COURTIER/**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(GET,"/api/Societe/societe/**").hasAuthority("ROLE_ADMINISTRATEUR")

                                                //Route gestion des membreAssurance par l'admin
                                                .requestMatchers(POST,"/api/MembreAssurance/creer/**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(GET,"/api/MembreAssurance/membre/**").hasAuthority("ROLE_ADMINISTRATEUR")




//                                                //Action et confidentialité des users de la DA et des Compagnies
//                                                .requestMatchers(PUT,"/api/DA/modifierMotDePasse/**").authenticated()


                                                //ROUTES DE GESTIONS DES NOTES DE CONJONCTURES

                                                // Voir toutes les notes de conjonctures de la base de données temporaire la où il pourra cliquer sur les bouton refuser et valider
                                                .requestMatchers(GET,"/api/IARD/ConjonctureIARDtemporaire/**").hasAnyAuthority("ROLE_EMPLOYEE","ROLE_ADMINISTRATEUR")
                                                .requestMatchers(GET,"/api/VIE/ConjonctureVIEtemporaire/**").hasAnyAuthority("ROLE_EMPLOYEE","ROLE_ADMINISTRATEUR")
                                                .requestMatchers(GET,"/api/COURTIER/ConjonctureCOURTIERtemporaire/**").hasAnyAuthority("ROLE_EMPLOYEE","ROLE_ADMINISTRATEUR")

                                                //Ici l'employé de la da peut faire une recherche dans la base de donnée temporaire en fonction de l'année de soumission cas pour cliquer sur refuser ou accepter

                                                .requestMatchers(GET,"/api/VIE/rechercheParDateSoumissionEmployee/{date}**").hasAuthority("ROLE_EMPLOYEE")
                                                .requestMatchers(GET,"/api/IARD/rechercheParDateSoumissionEmployee/{date}**").hasAuthority("ROLE_EMPLOYEE")
                                                .requestMatchers(GET,"/api/COURTIER/rechercheParDateSoumissionEmployee/{date}**").hasAuthority("ROLE_EMPLOYEE")

                                                //Ici l'employé de la da peut faire une recherche dans la base de donnée temporaire en fonction du nom de la societe cas pour cliquer sur refuser ou accepter
                                                .requestMatchers(GET,"/api/VIE/rechercheParSocieteEMPLOYE/{Nomsociete}**").hasAuthority("ROLE_EMPLOYEE")
                                                .requestMatchers(GET,"/api/IARD/rechercheParSocieteEMPLOYE/{Nomsociete}**").hasAuthority("ROLE_EMPLOYEE")
                                                .requestMatchers(GET,"/api/COURTIER/rechercheParSocieteEMPLOYE/{Nomsociete}**").hasAuthority("ROLE_EMPLOYEE")



                                                // Validation par l'employe pour  la base de donnée reelle,refuser envoyer notif

                                                .requestMatchers(POST,"/api/VIE/final/{dateSoumission}/{nomSociete}/{emailMembre}**").hasAuthority("ROLE_EMPLOYEE")
                                                .requestMatchers(POST,"/api/IARD/final/{dateSoumission}/{nomSociete}/{emailMembre}**").hasAuthority("ROLE_EMPLOYEE")
                                                .requestMatchers(POST,"/api/COURTIER/final/{dateSoumission}/{nomSociete}/{emailMembre}**").hasAuthority("ROLE_EMPLOYEE")






                                                // Accesssibilité et action de l'admin

                                                //Voir toutes les notes de conjonctures stockées dans la base de donnée reelle apres validation
                                                .requestMatchers(GET,"/api/IARD/ConjonctureIARDrelle/**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(GET,"/api/VIE/ConjonctureVIEreelle/**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(GET,"/api/COURTIER/ConjonctureCOURTIEreelle/**").hasAuthority("ROLE_ADMINISTRATEUR")

                                                 //Voir les notes en fonction de la date de soumission
                                                .requestMatchers(GET,"/api/VIE/rechercheParDateSoumission/{date}**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(GET,"/api/IARD/rechercheParDateSoumission/{date}**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(GET,"/api/IARD/rechercheParDateSoumission/{date}**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                //Voir les notes en fonction du nom de la compagnie
                                                .requestMatchers(GET,"/api/VIE/rechercheParSociete/{Nomsociete}**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(GET,"/api/IARD/rechercheParSociete/{Nomsociete}**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(GET,"/api/COURTIER/rechercheParSociete/{Nomsociete}**").hasAuthority("ROLE_ADMINISTRATEUR")

                                                //Supprimer les notes de conjonctures
                                                .requestMatchers(DELETE,"/api/SUPPRESSION/supprimerVIE/{dateSoumission}/{nom}/{emailMembre}**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(DELETE,"/api/SUPPRESSION/supprimerIARD/{dateSoumission}/{nom}/{emailMembre}**").hasAuthority("ROLE_ADMINISTRATEUR")
                                                .requestMatchers(DELETE,"/api/SUPPRESSION/supprimerCOURTIER/{dateSoumission}/{nom}/{emailMembre}**").hasAuthority("ROLE_ADMINISTRATEUR")

                                                // FIN ACTION ADMIN


                                                //Actions et Accessibilités des membres assurances

                                                //Remplir une note de conjoncture
                                                .requestMatchers(POST,"/api/IARD/creer/**").hasAuthority("TYPE_SOCIETE_IARD")
                                                .requestMatchers(POST,"/api/VIE/creer/**").hasAuthority("TYPE_SOCIETE_VIE")
                                                .requestMatchers(POST,"/api/COURTIER/creer/**").hasAuthority("TYPE_SOCIETE_COURTIER")

                                                //Afficher les notes de conjonctures créées par une compagnie bien definie

                                                .requestMatchers(GET,"/api/VIE/rechercheNoteParNomSociete/**").hasAuthority("TYPE_SOCIETE_VIE")
                                                .requestMatchers(GET,"/api/IARD/rechercheNoteParNomSociete/**").hasAuthority("TYPE_SOCIETE_IARD")
                                                .requestMatchers(GET,"/api/COURTIER/rechercheNoteParNomSociete/**").hasAuthority("TYPE_SOCIETE_COURTIER")


                                                // FIN ACTION COMPAGNIE
                                                //INFORMATION DASHBORD
                                                .requestMatchers(GET,"/api/dashbord/vu_ensemble/{annee}/{trimestre}**").permitAll()
                                                .requestMatchers(GET,"api/dashbord/IARD/{annee}/{trimestre}").permitAll()
                                                .requestMatchers(GET,"api/dashbord/VIE/{annee}/{trimestre}").permitAll()

                                                .requestMatchers(GET,"/api/dashbord/resultats**").hasAnyAuthority("ROLE_EMPLOYEE","ROLE_ADMINISTRATEUR")
                                                .requestMatchers(GET,"/api/dashbord/informations-note-conjoncture-par-societe").hasAuthority("ROLE_EMPLOYEE")
                                                .requestMatchers(GET,"/api/dashbord/informations-societe").authenticated()


                                                .requestMatchers(GET,"/api/dashbord/calculerTauxEvolutionVIE").permitAll()
                                                .requestMatchers(GET,"/api/dashbord/calculerTauxEvolutionIARD").permitAll()

                                                //calcul du taux d'evolution  total prime Vie et IARD pour chaque societe   annee successive
                                                .requestMatchers(GET,"/api/dashbord/calculerTauxEvolutionVIEAnnee").permitAll()
                                                .requestMatchers(GET,"/api/dashbord/calculerTauxEvolutionIARDAnnee").permitAll()

                                                //calcul du taux d'evolution  total prime Vie mm  trimestre  annee successive
                                                .requestMatchers(GET,"/api/dashbord/sumPrimeEmiseNetteAnnulationVIEAnnee").permitAll()

                                                //calcul du taux d'evolution  total prime IARD  mm  trimestre annee successive
                                                .requestMatchers(GET,"/api/dashbord/sumPrimeEmiseNetteAnnulationIARDAnnee").permitAll()

                                                //calcul du taux d'evolution  total prime Vie mm annee trimestre successif
                                                .requestMatchers(GET,"/api/dashbord/sumPrimeEmiseNetteAnnulationVIE").permitAll()

                                                //calcul du taux d'evolution  total prime IARD  mm annee trimestre successif
                                                .requestMatchers(GET,"/api/dashbord/sumPrimeEmiseNetteAnnulationIARD").permitAll()

                                                //calcul du taux d'evolution  total prime Vie et IARD confondu mm annee trimestre successif
                                                .requestMatchers(GET,"/api/dashbord/sumPrimeEmiseNetteAnnulationTotal").permitAll()

                                                //calcul du taux d'evolution total prime emise VIE et IARD confondu meme trimestre annee successive
                                                .requestMatchers(GET,"/api/dashbord/sumPrimeEmiseNetteAnnulationTotalAnnee").permitAll()

                                                //calcul du taux d'evolution  total sinistre IARD   meme trimestre annee successive
                                                .requestMatchers(GET,"/api/dashbord/sumSinistreIARDAnnee").permitAll()


                                                //calcul du taux d'evolution  total prestation VIE  meme trimestre annee successive
                                                .requestMatchers(GET,"/api/dashbord/sumprestationVIEAnnee").permitAll()

                                                //calcul du taux d'evolution  total prestation VIE et sinistre IARD  meme trimestre annee successive
                                                .requestMatchers(GET,"/api/dashbord/sumsinistreEtprestation").permitAll()

















                                                .anyRequest().authenticated()

                        )
                        .sessionManagement(httpSecuritySessionManagementConfigurer ->
                                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS) )
                        //.authenticationProvider()
                        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                        .build();
    }





    @Bean
    @Autowired
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();

    }

    @Bean
    public AuthenticationProvider authenticationProvider(@Qualifier("employeeService") UserDetailsService userDetailsService){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return  daoAuthenticationProvider;
    }
}
