package fr.meritis.first.service;

import fr.meritis.first.domain.Role;

public interface RoleService {
    Role getRoleByUserID(Long id);
}
