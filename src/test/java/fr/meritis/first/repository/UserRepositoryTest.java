package fr.meritis.first.repository;

import fr.meritis.first.domain.User;
import fr.meritis.first.exception.ApiException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional // or use @JpaDataTest (and remove @SpringBootTest and modify repository annotation in the repository implementation)to rollback changes after each test
public class UserRepositoryTest {

    public static final String EMAIL = "domain@fake.test";
    public static final String PASSWORD = "123456";
    public static final String FIRSTNAME = "toto";
    public static final String LASTNAME = "titi";
    public static final String PHONE = "12132154679";

    public UserRepositoryTest() {
    }

    @Autowired
    public UserRepository<User> userRepository;

    @Test
    public void createUserTest() {
        //Given
        User user = User.builder().email(EMAIL).password(PASSWORD).firstname(FIRSTNAME).lastname(LASTNAME)
                .phone(PHONE).isUsingMfa(false).enabled(true).isNotLocked(true).build();
        //When
        userRepository.create(user);
        //Then
        assertThat(user.getId()).isGreaterThan(0);
    }

    @Test
    public void loadUserByEmailTest() {
        //Given
        assertThatThrownBy(() -> userRepository.getUserByEmail(EMAIL)).isInstanceOf(ApiException.class); //TODO change exception throwed to the real exception
        User user = User.builder().email(EMAIL).password(PASSWORD).firstname(FIRSTNAME).lastname(LASTNAME)
                .phone(PHONE).isUsingMfa(false).enabled(true).isNotLocked(true).build();
        User user1 = User.builder().email(EMAIL + 1).password(PASSWORD + 1).firstname(FIRSTNAME + 1).lastname(LASTNAME + 1)
                .phone(PHONE + 1).isUsingMfa(false).enabled(true).isNotLocked(true).build();
        User user2 = User.builder().email(EMAIL + 2).password(PASSWORD + 2).firstname(FIRSTNAME + 2).lastname(LASTNAME + 2)
                .phone(PHONE + 2).isUsingMfa(false).enabled(true).isNotLocked(true).build();
        userRepository.create(user);
        userRepository.create(user1);
        userRepository.create(user2);

        //When
        User loadedUser =  userRepository.getUserByEmail(user.getEmail());

        //Then
        assertThat(loadedUser).isNotNull();
        assertThat(loadedUser.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void alreadyExistUserWithSameEmailTest(){
        //TODO for this moment this functionality is not established so i will do the test later
    }
    @Test
    public void emptyPasswordCreationUserTest(){
        //Given
        User user = User.builder().email(EMAIL).firstname(FIRSTNAME).lastname(LASTNAME)
                .phone(PHONE).isUsingMfa(false).enabled(true).isNotLocked(true).build();
        //When //Then
        assertThatThrownBy(() -> userRepository.getUserByEmail(EMAIL)).isInstanceOf(ApiException.class); //TODO change exception throwed to the real exception
    }
    @Test
    public void emptyEmailCreationUserTest(){
        User user = User.builder().password(PASSWORD).firstname(FIRSTNAME).lastname(LASTNAME)
                .phone(PHONE).isUsingMfa(false).enabled(true).isNotLocked(true).build();
        //When //Then
        assertThatThrownBy(() -> userRepository.getUserByEmail(EMAIL)).isInstanceOf(ApiException.class); //TODO change exception throwed to the real exception
    }

}
