package fr.meritis.first.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
        private Long id;
        private String firstname;
        private String lastname;
        private String email;
        private String address;
        private String phone;
        private String title;
        private String bio;
        private String imageUrl;
        private boolean enabled;
        private boolean isNotLocked;
        private boolean isUsingMfa;
        private LocalDateTime createdAt;
        private String roleName;
        private String permission;
}
