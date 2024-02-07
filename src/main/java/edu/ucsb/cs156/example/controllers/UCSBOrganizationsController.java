package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "UCSBOrganizations")
@RequestMapping("/api/ucsborganizations")
@RestController
@Slf4j
public class UCSBOrganizationsController extends ApiController {

    @Autowired
    UCSBOrganizationRepository ucsbOrganizationRepository;

    @Operation(summary= "List all ucsb organizations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBOrganization> allUCSBOrganizations() {
        Iterable<UCSBOrganization> orgs = ucsbOrganizationRepository.findAll();
        return orgs;
    }

    @Operation(summary= "Create a new organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBOrganization postUCSBOrganization(
            @Parameter(name="orgCode") @RequestParam String orgCode,
            @Parameter(name="orgTranslationShort") @RequestParam String orgTranslationShort,
            @Parameter(name="orgTranslation") @RequestParam String orgTranslation,
            @Parameter(name="inactive") @RequestParam boolean inactive)
            throws JsonProcessingException {

        UCSBOrganization ucsbOrg = new UCSBOrganization();
        ucsbOrg.setOrgCode(orgCode);
        ucsbOrg.setOrgTranslationShort(orgTranslationShort);
        ucsbOrg.setOrgTranslation(orgTranslation);
        ucsbOrg.setInactive(inactive);

        UCSBOrganization savedUcsbOrg = ucsbOrganizationRepository.save(ucsbOrg);

        return savedUcsbOrg;
    }

    @Operation(summary= "Get a single organization")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBOrganization getById(
            @Parameter(name="orgCode") @RequestParam String orgCode) {
        UCSBOrganization ucsbOrg = ucsbOrganizationRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

        return ucsbOrg;
    }

    @Operation(summary= "Delete a UCSBOrganization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteUCSBOrganization(
            @Parameter(name="orgCode") @RequestParam String orgCode) {
        UCSBOrganization ucsbOrg = ucsbOrganizationRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

        ucsbOrganizationRepository.delete(ucsbOrg);
        return genericMessage("UCSBOrganization with orgCode %s deleted".formatted(orgCode));
    }

    @Operation(summary= "Update a single UCSBOrganization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBOrganization updateUCSBOrganization(
            @Parameter(name="orgCode") @RequestParam String orgCode,
            @RequestBody @Valid UCSBOrganization incoming) {

        UCSBOrganization ucsbOrg = ucsbOrganizationRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

        ucsbOrg.setOrgTranslationShort(incoming.getOrgTranslationShort());
        ucsbOrg.setOrgTranslation(incoming.getOrgTranslation());
        ucsbOrg.setInactive(incoming.getInactive());

        ucsbOrganizationRepository.save(ucsbOrg);

        return ucsbOrg;
    }
}
