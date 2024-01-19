package fr.meritis.first.repository.implementation;

import fr.meritis.first.domain.Role;
import fr.meritis.first.exception.ApiException;
import fr.meritis.first.repository.RoleRepository;
import fr.meritis.first.repository.implementation.rowmapper.RoleRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;

import static fr.meritis.first.query.RoleQuery.*;
import static java.util.Map.of;
import static java.util.Objects.requireNonNull;


@Repository
@RequiredArgsConstructor
@Slf4j
public class RoleRepositoryImpl implements RoleRepository<Role> {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Override
    public Role create(Role data) {
        return null;
    }

    @Override
    public Collection<Role> list(int page, int pageSize) {
        return null;
    }

    @Override
    public Role get(Long id) {
        return null;
    }

    @Override
    public Role update(Role data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    @Override
    public void addRoleToUser(Long userId, String roleName) {
        log.info("Adding role {} to user id {}", roleName, userId);
        try {
            Role role = namedParameterJdbcTemplate.queryForObject(SELECT_ROLE_BY_NAME_QUERY, of("roleName", roleName),new RoleRowMapper());
            namedParameterJdbcTemplate.update(INSERT_ROLE_TO_USER_QUERY, of("userId", userId, "roleId", requireNonNull(role).getId()));
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No role found by name: " + roleName);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occure. Please try again.");
        }
    }

    @Override
    public Role getRoleByUserId(Long userId) {
        log.info("fetch role for user id {}", userId);
        try {
            return namedParameterJdbcTemplate.queryForObject(SELECT_ROLE_BY_USER_ID_QUERY, of("userId", userId),new RoleRowMapper());
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No role found by user id: " + userId);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occure. Please try again.");
        }
    }

    @Override
    public Role getRoleByUserEmail(String email) {
        return null;
    }

    @Override
    public void updateUserRole(Long userId, String roleName) {
    }
}
