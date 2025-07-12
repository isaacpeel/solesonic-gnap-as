package com.example.gnap.as.controller;

import com.example.gnap.as.model.GrantRequest;
import com.example.gnap.as.model.Interaction;
import com.example.gnap.as.service.GrantService;
import com.example.gnap.as.service.InteractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Optional;

/**
 * Controller for interaction management in the GNAP protocol.
 */
@Controller
@RequestMapping("/gnap/interact")
public class InteractionController {

    private static final Logger log = LoggerFactory.getLogger(InteractionController.class);

    private final InteractionService interactionService;
    private final GrantService grantService;

    public InteractionController(InteractionService interactionService, GrantService grantService) {
        this.interactionService = interactionService;
        this.grantService = grantService;
    }

    /**
     * Handle redirect interaction.
     *
     * @param grantId the grant ID
     * @param model the model
     * @return the consent page
     */
    @GetMapping("/redirect/{grantId}")
    public String handleRedirect(@PathVariable String grantId, Model model) {
        log.info("Received redirect interaction for grant: {}", grantId);

        Optional<GrantRequest> grant = grantService.findById(grantId);
        if (grant.isEmpty()) {
            model.addAttribute("error", "Grant not found");
            return "error";
        }

        List<Interaction> interactions = interactionService.findActiveInteractions(grantId);
        if (interactions.isEmpty()) {
            model.addAttribute("error", "No active interactions found");
            return "error";
        }

        // Add grant and interactions to model for consent page
        model.addAttribute("grant", grant.get());
        model.addAttribute("interactions", interactions);

        return "consent";
    }

    /**
     * Handle app interaction.
     *
     * @param grantId the grant ID
     * @return the app launch information
     */
    @GetMapping("/app/{grantId}")
    @ResponseBody
    public ResponseEntity<AppLaunchInfo> handleApp(@PathVariable String grantId) {
        log.info("Received app interaction for grant: {}", grantId);

        Optional<GrantRequest> grant = grantService.findById(grantId);
        if (grant.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Interaction> interactions = interactionService.findActiveInteractions(grantId);
        if (interactions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Create app launch info
        AppLaunchInfo appLaunchInfo = new AppLaunchInfo();
        appLaunchInfo.setGrantId(grantId);
        appLaunchInfo.setClientName(grant.get().getClient() != null ? 
                grant.get().getClient().getDisplayName() : "Unknown Client");

        return ResponseEntity.ok(appLaunchInfo);
    }

    /**
     * Handle user code interaction.
     *
     * @param grantId the grant ID
     * @param model the model
     * @return the user code page
     */
    @GetMapping("/user-code/{grantId}")
    public String handleUserCode(@PathVariable String grantId, Model model) {
        log.info("Received user code interaction for grant: {}", grantId);

        Optional<GrantRequest> grant = grantService.findById(grantId);
        if (grant.isEmpty()) {
            model.addAttribute("error", "Grant not found");
            return "error";
        }

        List<Interaction> interactions = interactionService.findActiveInteractions(grantId);
        if (interactions.isEmpty()) {
            model.addAttribute("error", "No active interactions found");
            return "error";
        }

        // Generate user code
        String userCode = generateUserCode();
        model.addAttribute("userCode", userCode);
        model.addAttribute("grant", grant.get());

        return "user-code";
    }

    /**
     * Handle interaction finish.
     *
     * @param grantId the grant ID
     * @param interactionId the interaction ID
     * @param hash the hash
     * @return redirect to client
     */
    @GetMapping("/finish/{grantId}")
    public RedirectView handleFinish(
            @PathVariable String grantId,
            @RequestParam String interactionId,
            @RequestParam String hash) {
        log.info("Received finish interaction for grant: {}, interaction: {}", grantId, interactionId);

        Optional<GrantRequest> grant = grantService.findById(grantId);
        if (grant.isEmpty() || grant.get().getRedirectUri() == null) {
            return new RedirectView("/error?message=Grant+not+found+or+no+redirect+URI");
        }

        // Validate interaction
        if (!interactionService.validateInteraction(grantId, interactionId, null)) {
            return new RedirectView("/error?message=Invalid+interaction");
        }

        // Update grant status to approved
        grantService.updateGrantStatus(grantId, GrantRequest.GrantStatus.APPROVED);

        // Redirect to client with hash
        String redirectUri = grant.get().getRedirectUri();
        if (redirectUri.contains("?")) {
            redirectUri += "&hash=" + hash;
        } else {
            redirectUri += "?hash=" + hash;
        }

        return new RedirectView(redirectUri);
    }

    /**
     * Submit consent form.
     *
     * @param grantId the grant ID
     * @param approved whether the consent was approved
     * @return redirect to finish or error
     */
    @PostMapping("/consent/{grantId}")
    public RedirectView submitConsent(
            @PathVariable String grantId,
            @RequestParam boolean approved) {
        log.info("Received consent submission for grant: {}, approved: {}", grantId, approved);

        Optional<GrantRequest> grant = grantService.findById(grantId);
        if (grant.isEmpty()) {
            return new RedirectView("/error?message=Grant+not+found");
        }

        List<Interaction> interactions = interactionService.findActiveInteractions(grantId);
        if (interactions.isEmpty()) {
            return new RedirectView("/error?message=No+active+interactions+found");
        }

        // Update grant status based on consent
        GrantRequest.GrantStatus status = approved ? 
                GrantRequest.GrantStatus.APPROVED : GrantRequest.GrantStatus.DENIED;
        grantService.updateGrantStatus(grantId, status);

        if (approved) {
            // Redirect to finish endpoint
            Interaction interaction = interactions.getFirst();
            String hash = "approved"; // In a real implementation, this would be a proper hash
            return new RedirectView("/gnap/interact/finish/" + grantId + 
                    "?interactionId=" + interaction.getId() + "&hash=" + hash);
        } else {
            // Redirect to error page
            return new RedirectView("/error?message=Consent+denied");
        }
    }

    /**
     * Generate a user code.
     *
     * @return the generated user code
     */
    private String generateUserCode() {
        // Generate a 6-digit code
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    /**
     * App launch information.
     */
    public static class AppLaunchInfo {
        private String grantId;
        private String clientName;

        public String getGrantId() {
            return grantId;
        }

        public void setGrantId(String grantId) {
            this.grantId = grantId;
        }

        public String getClientName() {
            return clientName;
        }

        public void setClientName(String clientName) {
            this.clientName = clientName;
        }
    }
}
