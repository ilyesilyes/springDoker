package fr.meritis.first.service.implementation;

import fr.meritis.first.domain.Role;
import fr.meritis.first.repository.RoleRepository;
import fr.meritis.first.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository<Role> roleRepository;
    @Override
    public Role getRoleByUserID(Long id) {
        return roleRepository.getRoleByUserId(id);
    }
}
