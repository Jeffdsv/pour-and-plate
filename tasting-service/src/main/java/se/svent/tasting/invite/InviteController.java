package se.svent.tasting.invite;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import se.svent.tasting.user.UserResponse;

@RestController
@RequestMapping("/api/invites")
public class InviteController {

    private final InviteService service;

    public InviteController(InviteService service) {
        this.service = service;
    }

    // POST /api/invites makes a new link, only the admin may do this, see SecurityConfig
    @PostMapping
    public InviteResponse create(Authentication authentication) {
        return service.create(authentication.getName());
    }

    // GET /api/invites/abc123 tells the sign up page if the link still works
    @GetMapping("/{token}")
    public InviteStatusResponse check(@PathVariable String token) {
        return new InviteStatusResponse(service.isValid(token));
    }

    // POST /api/invites/abc123/accept creates the friend account
    @PostMapping("/{token}/accept")
    public UserResponse accept(@PathVariable String token, @Valid @RequestBody AcceptInviteRequest request) {
        return service.accept(token, request);
    }
}