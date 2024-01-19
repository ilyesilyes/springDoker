package fr.meritis.first.service.implementation;

import fr.meritis.first.domain.Role;
import fr.meritis.first.domain.User;
import fr.meritis.first.dto.UserDTO;
import fr.meritis.first.repository.RoleRepository;
import fr.meritis.first.repository.UserRepository;
import fr.meritis.first.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static fr.meritis.first.dto.dtomapper.UserDTOMapper.fromUser;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository<User> userRepository;
    private final RoleRepository<Role> roleRepository;
    @Override
    public UserDTO createUser(User user) {
        return fromUser(userRepository.create(user));
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        return mapToUserDTO(userRepository.getUserByEmail(email));
    }

    @Override
    public void sendVerificationCode(UserDTO user) {
        userRepository.sendVerificationCode(user);
    }

    @Override
    public UserDTO verifyCode(String email, String code) {
        return mapToUserDTO(userRepository.verifyCode(email, code));
    }

    @Override
    public void resetPassword(String email) {
        userRepository.resetPassword(email);
    }

    @Override
    public UserDTO verifyPasswordKey(String key) {
        return mapToUserDTO(userRepository.verifyPasswordKey(key));
    }

    @Override
    public void renewPassword(String key, String password, String confirmPassword) {
        userRepository.renewPassword(key, password, confirmPassword);
    }

    private UserDTO mapToUserDTO(User user) {;
    return fromUser(user, roleRepository.getRoleByUserId(user.getId()));
    }

    @Override
    public UserDTO verifyAccountKey(String key) {
        return mapToUserDTO(userRepository.verifyAccountKey(key));
    }

}
