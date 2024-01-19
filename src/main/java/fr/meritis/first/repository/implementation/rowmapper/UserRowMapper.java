package fr.meritis.first.repository.implementation.rowmapper;

import fr.meritis.first.domain.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {

        return User.builder()
                .id(rs.getLong("id"))
                .firstname(rs.getString("first_name"))
                .lastname(rs.getString("last_name"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .address(rs.getString("address"))
                .phone(rs.getString("phone"))
                .title(rs.getString("title"))
                .bio(rs.getString("bio"))
                .imageUrl(rs.getString("image_url"))
                .enabled(rs.getBoolean("enabled"))
                .isUsingMfa(rs.getBoolean("using_mfa"))
                .isNotLocked(rs.getBoolean("non_locked"))
                .createdAt(rs.getTimestamp("created_date").toLocalDateTime())
                .build();

    }
}
