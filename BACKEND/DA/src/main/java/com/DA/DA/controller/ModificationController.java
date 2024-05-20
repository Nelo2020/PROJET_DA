package com.DA.DA.controller;

import com.DA.DA.dto.modificationDTO1;
import com.DA.DA.dto.modificationSocieteDTO;
import com.DA.DA.entite.Da;
import com.DA.DA.entite.MembreAssurance;
import com.DA.DA.entite.Role;
import com.DA.DA.repository.DaRepository;
import com.DA.DA.repository.MembreAssuranceRepository;
import com.DA.DA.service.EmployeeService;
import com.DA.DA.service.SocieteService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/MODIFICATION")
public class ModificationController {

    @Autowired
    private DaRepository daRepository;
    @Autowired
    private MembreAssuranceRepository membreAssuranceRepository;
    private   PasswordEncoder passwordEncoder;
    private JavaMailSender mailSender;
    @Autowired
    EmployeeService employeeService;
    SocieteService societeService;


//
    @PutMapping("/modifier/Employe_DA")
    public ResponseEntity<String> modifierCompteEmploye(@RequestBody modificationDTO1 modificationDTO) {
        return employeeService.modifierCompte(modificationDTO);
    }

    @PutMapping("/modifier/Membre")
    public ResponseEntity<String> modifierCompteMenbre(@RequestBody modificationDTO1 modificationDTO) {
        return employeeService.modifierMotDePasse(modificationDTO);
    }
    //Modifier le siege,dirigeant,nom,numero RCC
    @PutMapping("/modifier/SOCIETE")
    public ResponseEntity<String> modifierCompteSociete(@RequestBody modificationSocieteDTO modificationDTO) {
        return societeService.modifier(modificationDTO);
    }
}


