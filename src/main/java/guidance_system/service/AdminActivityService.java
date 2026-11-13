
package guidance_system.service;

import guidance_system.entity.Admin;
import guidance_system.entity.AdminActivity;
import guidance_system.repository.AdminActivityRepository;
import guidance_system.repository.AdminRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminActivityService {

    private final AdminActivityRepository activityRepository;
    private final AdminRepository adminRepository;

    public AdminActivityService(
            AdminActivityRepository activityRepository,
            AdminRepository adminRepository) {
        this.activityRepository = activityRepository;
        this.adminRepository = adminRepository;
    }

    @Transactional
    public void logActivity(
            String actionType,
            String description,
            String entityType,
            Long entityId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new IllegalStateException(
                    "Authenticated administrator not found."
            );
        }

        Admin admin = adminRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Admin account not found."
                        ));

        AdminActivity activity = new AdminActivity();
        activity.setAdmin(admin);
        activity.setActionType(actionType);
        activity.setDescription(description);
        activity.setEntityType(entityType);
        activity.setEntityId(entityId);

        activityRepository.save(activity);
    }
}
