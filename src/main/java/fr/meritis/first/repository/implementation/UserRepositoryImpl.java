package fr.meritis.first.repository.implementation;

import fr.meritis.first.domain.Role;
import fr.meritis.first.domain.User;
import fr.meritis.first.domain.UserPrincipal;
import fr.meritis.first.dto.UserDTO;
import fr.meritis.first.enumeration.VerificationType;
import fr.meritis.first.exception.ApiException;
import fr.meritis.first.repository.RoleRepository;
import fr.meritis.first.repository.UserRepository;
import fr.meritis.first.repository.implementation.rowmapper.UserRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import static fr.meritis.first.enumeration.RoleType.ROLE_USER;
import static fr.meritis.first.enumeration.VerificationType.ACCOUNT;
import static fr.meritis.first.enumeration.VerificationType.PASSWORD;
import static fr.meritis.first.query.UserQuery.*;
import static fr.meritis.first.utils.SmsUtils.sendSMS;
import static java.util.Map.of;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.time.DateFormatUtils.format;
import static org.apache.commons.lang3.time.DateUtils.addDays;


@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository<User>, UserDetailsService {
    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RoleRepository<Role> roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    @Override
    public User create(User user) {
        //check the email is unique
        if(getEmailCount(user.getEmail().trim().toLowerCase()) > 0) throw new ApiException("Email already in use. please use a different email and try again.");
        //same new user
        try {
            KeyHolder holder = new GeneratedKeyHolder();
            SqlParameterSource parameter = getSqlParameterSource(user);
            namedParameterJdbcTemplate.update(INSERT_USER_QUERRY, parameter, holder);
            Long id = (Long) (holder.getKeyList().get(0).getOrDefault("ID", null));
            if (isNull(id)) id = requireNonNull(holder.getKey()).longValue();
            user.setId(id);
            roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());

            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());
            namedParameterJdbcTemplate.update(INSERT_VERIFICATION_URL_QUERY, of("userId", user.getId(), "url", verificationUrl));

            //emailService.sendVerificationUrl(user.getFirstname(), user.getEmail(), verificationUrl, ACCOUNT);

            user.setEnabled(false);
            user.setNotLocked(true);

            return user;
        } catch (Exception exception) {
            log.error("An exception occurred :", exception);
            throw new ApiException("An error occurred please try again." );
        }
    }

    @Override
    public Collection<User> list(int page, int pageSize) {
        return null;
    }

    @Override
    public User get(Long id) {
        return null;
    }

    @Override
    public User update(User data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = getUserByEmail(email);
        if (user == null) {
            log.info("User not found in the database: {}", email);
            throw new UsernameNotFoundException(String.format("User not found in the database: %s", email));
        } else {
            log.info("User found in the database: {}", email);
            return new UserPrincipal(user, roleRepository.getRoleByUserId(user.getId()));
        }
    }

    public User getUserByEmail(String email) {
        try {
            return namedParameterJdbcTemplate.queryForObject(SELECT_USER_BY_EMAIL_QUERRY, of("email", email), new UserRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            throw new ApiException("No user found by email:" + email);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new ApiException("An error eccurred. Please try again.");
        }

    }

    @Override
    public void sendVerificationCode(UserDTO user) {
        String experationDate = format(addDays(new Date(),1), DATE_FORMAT);
        String verificaionCode = randomAlphabetic(8).toUpperCase();
        try {
            namedParameterJdbcTemplate.update(DELETE_VERIFICATION_CODE_BY_USER_ID, of("userId", user.getId()));
            namedParameterJdbcTemplate.update(INSERT_VERIFICATION_CODE_QUERRY, of("userId", user.getId(), "code",verificaionCode, "expirationDate", experationDate));
            sendSMS(user.getPhone(),"From: first\nVerification code \n" + verificaionCode);
            log.info("Verification code: {}", verificaionCode);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new ApiException("An error eccurred. Please try again.");
        }

    }

    @Override
    public User verifyCode(String email, String code) {
        if(isVerificationCodeIsExpired(code)) throw new ApiException("This code is expired. Please login again.");

        try {
            User userByCode = namedParameterJdbcTemplate.queryForObject(SELECT_USER_BY_USER_CODE_QUERRY_FROM_TWO_FACTOR_VERFICATION, of("code", code), new UserRowMapper());
            User userByEmail = namedParameterJdbcTemplate.queryForObject(SELECT_USER_BY_EMAIL_QUERRY, of("email", email), new UserRowMapper());
            if (userByCode.getEmail().equalsIgnoreCase(userByEmail.getEmail())) {
                namedParameterJdbcTemplate.update(DELETE_CODE_FROM_TWO_FACTOR_AUTHENTIFICATION, of("code", code));
                return userByCode;
            } else {
                throw new ApiException("Code is invalid. Please try again.");
            }
        } catch(EmptyResultDataAccessException exception) {
            throw new ApiException("Could not find record.");
        } catch(Exception exception) {
            throw new ApiException("An error eccurred. Please try again.");
        }
    }

    @Override
    public void resetPassword(String email) {
        if (getEmailCount(email.trim().toLowerCase()) == 0)  throw new ApiException("There is no account for this email.");
        try {
                String expirationDate = format(addDays(new Date(), 1), DATE_FORMAT);
                User user = getUserByEmail(email);
                String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), PASSWORD.getType());
                namedParameterJdbcTemplate.update(DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY, of("userId", user.getId()));
                namedParameterJdbcTemplate.update(INSERT_PASSWORD_VERIFICATION_QUERY, of("userId", user.getId(), "url", verificationUrl, "expirationDate", expirationDate));
                // TODO send email with url to user
                log.info("Verification URL: {}", verificationUrl);
        } catch(Exception exception) {
            throw new ApiException("An error eccurred. Please try again.");
        }
    }

    @Override
    public User verifyPasswordKey(String key) {
        if (isLinkExpired(key, PASSWORD)) throw new ApiException("This link has expired. please reset your password again.");
        try {
            User user = namedParameterJdbcTemplate.queryForObject(SELECT_USER_BY_PASSWORD_URL_QUERY, of("url", getVerificationUrl(key, PASSWORD.getType())), new UserRowMapper());
            //namedParameterJdbcTemplate.update("DELETE_USER_FROM_PASSWORD_VERIFICATION_QUERY", of("id", user.getId())); // Depends on use case / developer or business
            return user;
        } catch(EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("This link is not valid. Please reset your password again");
        } catch(Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error eccurred. Please try again.");
        }
    }

    @Override
    public void renewPassword(String key, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) throw new ApiException("Password don't match. Please try again.");
        try {
            namedParameterJdbcTemplate.update(UPDATE_USER_PASSWORD_BY_URL_QUERY, of("password", bCryptPasswordEncoder.encode(password), "url", getVerificationUrl(key, PASSWORD.getType())));
            namedParameterJdbcTemplate.update(DELETE_VERIFICATION_BY_URL_QUERY, of("url", getVerificationUrl(key, PASSWORD.getType())));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occured. Please try again.");
        }
    }

    @Override
    public User verifyAccountKey(String key) {
        try {
            User user = namedParameterJdbcTemplate.queryForObject(SELECT_USER_BY_ACCOUNT_URL_QUERY, of("url", getVerificationUrl(key, ACCOUNT.getType())), new UserRowMapper());
            namedParameterJdbcTemplate.update(UPDATE_USER_ENABLED_QUERY, of("enabled", true, "userId", user.getId()));
            //delete after updating - depends on your requirement
            return user;
        } catch(EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("This link is not valid.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occured. Please try again.");
        }
    }

    private Boolean isLinkExpired(String key, VerificationType password) {
        try {
            return namedParameterJdbcTemplate.queryForObject(SELECT_EXPIRATION_BY_URL_QUERY, of("url", getVerificationUrl(key, password.getType())), Boolean.class);
        } catch(EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("This link is not valid. Please reset your password again");
        } catch(Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error eccurred. Please try again.");
        }
    }

    private String getVerificationUrl(String key, String type) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/verify/" + type + "/" + key).toUriString();
    }

    private SqlParameterSource getSqlParameterSource(User user) {
        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstname())
                .addValue("lastName", user.getLastname())
                .addValue("email", user.getEmail())
                .addValue("password", bCryptPasswordEncoder.encode(user.getPassword()));
    }

    private Integer getEmailCount(String email) {
        return namedParameterJdbcTemplate.queryForObject(COUNT_USER_EMAIL_QUERY, of("email", email), Integer.class);
    }

    private Boolean isVerificationCodeIsExpired(String code) {
        try {
            return namedParameterJdbcTemplate.queryForObject(SELECT_CODE_EXPERATION_QUERY, of("code", code), Boolean.class);
        } catch(EmptyResultDataAccessException exception) {
            throw new ApiException("This code is not valid.");
        } catch(Exception exception) {
            throw new ApiException("An error eccurred. Please try again.");
        }
    }
}
