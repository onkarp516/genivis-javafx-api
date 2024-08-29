package in.truethics.ethics.ethicsapiv10.model.access_permissions;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "role_access_permissions_tbl")
public class RoleAccessPermission {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "role_id")
        @JsonManagedReference
        private UserRole userRole;

        @ManyToOne
        @JoinColumn(name = "action_mapping_id")
        @JsonManagedReference
        private SystemActionMapping systemActionMapping;

        @Column(name = "created_by")
        private Long createdBy;
        @CreationTimestamp
        @Column(name = "created_at")
        private LocalDateTime createdAt;
        private Boolean status;
        @Column(name = "user_actions_id")
        private String userActionsId;//System Master Actions Id


}
