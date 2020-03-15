package com.redroundrobin.thirema.apirest;

import com.redroundrobin.thirema.apirest.models.postgres.Users;
import com.redroundrobin.thirema.apirest.repository.postgres.UsersRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=ApirestApplication.class)
@EnableTransactionManagement
public class JPAMultipleDBTest {

    @Autowired
    private UsersRepository usersRepository;

    //@Autowired
    //private SensorsRepository sensorsRepository;

    // tests

    @Test
    @Transactional("postgresTransactionManager")
    public void whenCreatingUser_thenCreated() {
        Users user = new Users();
        user.setUser_id(1);
        user.setName("user1");
        user.setPassword("pass");
        user.setEmail("user1@test.com");
        user = usersRepository.save(user);

        assertTrue(usersRepository.findById(user.getUser_id()).isPresent());
    }
/*
    @Test
    @Transactional("userTransactionManager")
    public void whenCreatingUsersWithSameEmail_thenRollback() {
        UserMultipleDB user1 = new UserMultipleDB();
        user1.setName("John");
        user1.setEmail("john@test.com");
        user1.setAge(20);
        user1 = userRepository.save(user1);
        assertTrue(userRepository.findById(user1.getId()).isPresent());

        UserMultipleDB user2 = new UserMultipleDB();
        user2.setName("Tom");
        user2.setEmail("john@test.com");
        user2.setAge(10);
        try {
            user2 = userRepository.save(user2);
            userRepository.flush();
            fail("DataIntegrityViolationException should be thrown!");
        } catch (final DataIntegrityViolationException e) {
            // Expected
        } catch (final Exception e) {
            fail("DataIntegrityViolationException should be thrown, instead got: " + e);
        }
    }

    @Test
    @Transactional("productTransactionManager")
    public void whenCreatingProduct_thenCreated() {
        Product product = new Product();
        product.setName("Book");
        product.setId(2);
        product.setPrice(20);
        product = productRepository.save(product);

        assertTrue(productRepository.findById(product.getId()).isPresent());
    }
*/
}
