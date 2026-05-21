package com.examsystem.repository;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.examsystem.model.User;
import com.examsystem.db.DatabaseConnection;

import static org.junit.Assert.*;

/**
 * Unit tests for UserRepository implementation.
 * Tests CRUD operations for User entity.
 */
public class UserRepositoryTest {
    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryTest.class);
    private static UserRepository userRepository;

    @BeforeClass
    public static void setUp() {
        logger.info("Setting up UserRepository tests");
        userRepository = new UserRepositoryImpl();
    }

    @Test
    public void testDatabaseConnection() {
        logger.info("Testing database connection");
        assertTrue("Database connection should be successful", DatabaseConnection.testConnection());
    }

    @Test
    public void testSaveUser() {
        logger.info("Testing save user operation");
        User user = new User("testuser", "password123", "test@example.com", "Test User", User.UserRole.STUDENT);

        try {
            userRepository.save(user);
            assertNotNull("User ID should be set after save", user.getUserId());
            logger.info("User saved successfully with ID: {}", user.getUserId());
        } catch (Exception e) {
            logger.error("Error saving user", e);
            fail("Failed to save user: " + e.getMessage());
        }
    }

    @Test
    public void testFindUserById() {
        logger.info("Testing find user by ID");
        try {
            // Create a test user first
            User testUser = new User("findbyid", "password", "findbyid@test.com", "Find Test", User.UserRole.TEACHER);
            userRepository.save(testUser);

            // Try to find it
            var foundUser = userRepository.findById(testUser.getUserId());
            assertTrue("User should be found", foundUser.isPresent());
            assertEquals("Username should match", "findbyid", foundUser.get().getUsername());
            logger.info("User found successfully");
        } catch (Exception e) {
            logger.error("Error in findById test", e);
            fail("Failed to find user: " + e.getMessage());
        }
    }

    @Test
    public void testFindUserByUsername() {
        logger.info("Testing find user by username");
        try {
            User testUser = new User("uniqueuser", "pass", "unique@test.com", "Unique Test", User.UserRole.STUDENT);
            userRepository.save(testUser);

            var foundUser = userRepository.findByUsername("uniqueuser");
            assertTrue("User should be found by username", foundUser.isPresent());
            assertEquals("Email should match", "unique@test.com", foundUser.get().getEmail());
            logger.info("User found by username successfully");
        } catch (Exception e) {
            logger.error("Error in findByUsername test", e);
            fail("Failed to find user by username: " + e.getMessage());
        }
    }

    @Test
    public void testExistsByUsername() {
        logger.info("Testing existsByUsername");
        try {
            User testUser = new User("checkuser", "pass", "check@test.com", "Check Test", User.UserRole.ADMIN);
            userRepository.save(testUser);

            assertTrue("User should exist", userRepository.existsByUsername("checkuser"));
            assertFalse("Non-existent user should return false", userRepository.existsByUsername("nonexistent"));
            logger.info("existsByUsername test passed");
        } catch (Exception e) {
            logger.error("Error in existsByUsername test", e);
            fail("Failed existsByUsername test: " + e.getMessage());
        }
    }

    @Test
    public void testUpdateUser() {
        logger.info("Testing update user");
        try {
            User testUser = new User("updatetest", "pass", "update@test.com", "Update Test", User.UserRole.STUDENT);
            userRepository.save(testUser);

            // Update the user
            testUser.setEmail("newemail@test.com");
            testUser.setFullName("Updated Name");
            userRepository.update(testUser);

            // Verify update
            var updatedUser = userRepository.findById(testUser.getUserId());
            assertTrue("User should be found", updatedUser.isPresent());
            assertEquals("Email should be updated", "newemail@test.com", updatedUser.get().getEmail());
            logger.info("User updated successfully");
        } catch (Exception e) {
            logger.error("Error in update test", e);
            fail("Failed to update user: " + e.getMessage());
        }
    }

    @Test
    public void testCountUsers() {
        logger.info("Testing count users");
        try {
            long countBefore = userRepository.count();
            User testUser = new User("counttest", "pass", "count@test.com", "Count Test", User.UserRole.STUDENT);
            userRepository.save(testUser);

            long countAfter = userRepository.count();
            assertEquals("Count should increase by 1", countBefore + 1, countAfter);
            logger.info("Count test passed: {} -> {}", countBefore, countAfter);
        } catch (Exception e) {
            logger.error("Error in count test", e);
            fail("Failed count test: " + e.getMessage());
        }
    }

    @Test
    public void testFindAll() {
        logger.info("Testing find all users");
        try {
            var allUsers = userRepository.findAll();
            assertNotNull("User list should not be null", allUsers);
            assertTrue("User list should not be empty", allUsers.size() > 0);
            logger.info("Found {} users", allUsers.size());
        } catch (Exception e) {
            logger.error("Error in findAll test", e);
            fail("Failed to find all users: " + e.getMessage());
        }
    }
}
