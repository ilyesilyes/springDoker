package fr.meritis.first.repository;

import fr.meritis.first.domain.Role;

import java.util.Collection;

public interface RoleRepository<T extends Role> {
    /* Basic CRUD Operation */
    T create(T data);
    Collection<T> list(int page, int pageSize);
    T get(Long id);
    T update(T data);
    Boolean delete(Long id);

    /* more complexe operation */

    void addRoleToUser(Long userId, String roleName);

    Role getRoleByUserId(Long userId);
    Role getRoleByUserEmail(String email);
    void updateUserRole(Long userId, String roleName);
}
