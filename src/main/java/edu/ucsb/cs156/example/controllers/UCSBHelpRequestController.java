package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.entities.UCSBDiningCommons;
import edu.ucsb.cs156.example.entities.UCSBHelpRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBHelpRequestRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalDateTime;

import javax.validation.Valid;

@Tag(name = "UCSBHelpRequest")
@RequestMapping("/api/ucsbhelprequest")
@RestController
@Slf4j

public class UCSBHelpRequestController extends ApiController {

    @Autowired
    UCSBHelpRequestRepository ucsbHelpRequestRepository;

    @Operation(summary= "List all help requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBHelpRequest> allHelpRequests() {
        Iterable<UCSBHelpRequest> requests = ucsbHelpRequestRepository.findAll();
        return requests;
    }
    
    @Operation(summary= "Create a help request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBHelpRequest postHelpRequest(
            @Parameter(name="requesterEmail") @RequestParam String requesterEmail,
            @Parameter(name="teamId") @RequestParam String teamId,
            @Parameter(name="tableOrBreakoutRoom") @RequestParam String tableOrBreakoutRoom,
            @Parameter(name="explanation") @RequestParam String explanation,
            @Parameter(name="solved") @RequestParam boolean solved,
            @Parameter(name="requestTime", description="date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("requestTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime requestTime)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        log.info("requestTime={}", requestTime);

        UCSBHelpRequest ucsbHelpRequest = new UCSBHelpRequest();
        ucsbHelpRequest.setRequesterEmail(requesterEmail);
        ucsbHelpRequest.setTeamId(teamId);
        ucsbHelpRequest.setTableOrBreakoutRoom(tableOrBreakoutRoom);
        ucsbHelpRequest.setExplanation(explanation);
        ucsbHelpRequest.setSolved(solved);
        ucsbHelpRequest.setRequestTime(requestTime);

        UCSBHelpRequest savedUcsbHelpRequest = ucsbHelpRequestRepository.save(ucsbHelpRequest);

        return savedUcsbHelpRequest;
    }

    @Operation(summary= "Get a single help request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBHelpRequest getById(
            @Parameter(name="id") @RequestParam Long id) {
                UCSBHelpRequest ucsbHelpRequest = ucsbHelpRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBHelpRequest.class, id));

        return ucsbHelpRequest;
    }

    @Operation(summary= "Delete a help request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteUCSBHelpRequest(
            @Parameter(name="id") @RequestParam Long id) {
                UCSBHelpRequest ucsbHelpRequest = ucsbHelpRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBHelpRequest.class, id));

                ucsbHelpRequestRepository.delete(ucsbHelpRequest);
        return genericMessage("UCSBHelpRequest with id %s deleted".formatted(id));
    }

    @Operation(summary= "Update a single help request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBHelpRequest updateUCSBHelpRequest(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid UCSBHelpRequest incoming) {

                UCSBHelpRequest ucsbHelpRequest = ucsbHelpRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBHelpRequest.class, id));

                ucsbHelpRequest.setRequesterEmail(incoming.getRequesterEmail());
                ucsbHelpRequest.setTeamId(incoming.getTeamId());
                ucsbHelpRequest.setTableOrBreakoutRoom(incoming.getTableOrBreakoutRoom());
                ucsbHelpRequest.setExplanation(incoming.getExplanation());
                ucsbHelpRequest.setSolved(incoming.getSolved());
                ucsbHelpRequest.setRequestTime(incoming.getRequestTime());

                ucsbHelpRequestRepository.save(ucsbHelpRequest);

        return ucsbHelpRequest;
    }
}
