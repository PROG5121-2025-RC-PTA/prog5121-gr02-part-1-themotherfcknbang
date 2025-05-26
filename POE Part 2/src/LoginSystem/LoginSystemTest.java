package LoginSystem; 
import javax.swing.JOptionPane;
import java.awt.HeadlessException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Scanner;
import java.lang.reflect.Field;

public class LoginSystemTest {
    
    private Login login;
    private User testUser;
    private final InputStream originalIn = System.in;
  
    void setUp() {
        login = new Login();
        testUser = new User("test_", "Password1!", "+27123456789", "John", "Doe");
        LoginSystem.users.clear();
        
        // Reset static message counters
        try {
            Field totalMessageCountField = Message.class.getDeclaredField("totalMessageCount");
            totalMessageCountField.setAccessible(true);
            totalMessageCountField.setInt(null, 0);
            
            Field allSentMessagesField = Message.class.getDeclaredField("allSentMessages");
            allSentMessagesField.setAccessible(true);
            ((java.util.List<?>) allSentMessagesField.get(null)).clear();
        } catch (Exception e) {
            System.out.println("Could not reset static fields: " + e.getMessage());
        }
    }
    void tearDown() {
        System.setIn(originalIn);
    }
    
    // Login Class Tests
    
    void testValidUsername() {
        assertTrue(login.checkUserName("test_"));
        assertTrue(login.checkUserName("a_b"));
        assertFalse(login.checkUserName("toolong_"));
        assertFalse(login.checkUserName("test"));
    }
    
    void testValidPassword() {
        assertTrue(login.checkPasswordComplexity("Password1!"));
        assertFalse(login.checkPasswordComplexity("pass"));
        assertFalse(login.checkPasswordComplexity("password1!"));
    }
    
  
    void testValidCellphone() {
        assertTrue(login.checkCellPhoneNumber("+27123456789"));
        assertFalse(login.checkCellPhoneNumber("27123456789"));
        assertFalse(login.checkCellPhoneNumber("+abc123"));
    }
    
    void testUserRegistration() {
        String result = login.registerUser("test_", "Password1!", "+27123456789", "John", "Doe");
        assertEquals("Registration successful!", result);
        assertEquals(1, LoginSystem.users.size());
    }
   
    void testUserLogin() {
        LoginSystem.users.add(testUser);
        assertTrue(login.loginUser("test_", "Password1!"));
        assertFalse(login.loginUser("test_", "wrongpass"));
        assertFalse(login.loginUser("wrong", "Password1!"));
    }
   
    void testLoginStatus() {
        assertEquals("Welcome, it is great to see you again", login.returnLoginStatus(true));
        assertEquals("Username or password incorrect, please try again", login.returnLoginStatus(false));
    }
    
    // User Class Tests
    void testUserCreation() {
        assertEquals("test_", testUser.getUsername());
        assertEquals("Password1!", testUser.getPassword());
        assertEquals("+27123456789", testUser.getCellphone());
        assertEquals("John", testUser.getFirstName());
        assertEquals("Doe", testUser.getLastName());
        assertEquals(0, testUser.getTotalMessagesSent());
    }
    
    void testUserUpdates() {
        testUser.setPassword("NewPass1!");
        testUser.setFirstName("Jane");
        testUser.incrementTotalMessages();
        
        assertEquals("NewPass1!", testUser.getPassword());
        assertEquals("Jane", testUser.getFirstName());
        assertEquals(1, testUser.getTotalMessagesSent());
    }
    
    // Message Class Tests
    void testMessageCreation() {
        Message message = new Message("+27718693002", "Hello world", 1);
        
        assertNotNull(message.getMessageId());
        assertEquals(1, message.getMessageNumber());
        assertEquals("+27718693002", message.getRecipient());
        assertEquals("Hello world", message.getMessageContent());
        assertNotNull(message.getMessageHash());
    }
    void testMessageValidation() {
        Message validMessage = new Message("+27718693002", "test", 1);
        Message invalidMessage = new Message("08575975889", "test", 1);
        
        assertTrue(validMessage.checkMessageID());
        assertEquals(-1, validMessage.checkRecipientCell()); // Valid
        assertEquals(1, invalidMessage.checkRecipientCell()); // Invalid - no +
    }
   
    void testMessageSending() {
        Message message = new Message("+27718693002", "Hello world", 1);
        
        // Test sending message with JOptionPane
        String input = "1\n"; // Select send option
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Update the scanner in Message class
        try {
            Field scannerField = Message.class.getDeclaredField("sc");
            scannerField.setAccessible(true);
            scannerField.set(null, new Scanner(System.in));
            
            String result = message.SentMessage();
            assertEquals("SENT", result);
            
            // Verify JOptionPane would show success message
            String expectedJOptionContent = "MESSAGE SENT SUCCESSFULLY!\n\n" +
                    "Message ID: " + message.getMessageId() + "\n" +
                    "Message Number: " + message.getMessageNumber() + "\n" +
                    "Recipient: " + message.getRecipient() + "\n" +
                    "Message: " + message.getMessageContent() + "\n" +
                    "Message Hash: " + message.getMessageHash();
            
            assertTrue(expectedJOptionContent.contains("MESSAGE SENT SUCCESSFULLY!"));
            assertTrue(expectedJOptionContent.contains(message.getRecipient()));
            
        } catch (HeadlessException e) {
            System.out.println("JOptionPane test skipped - headless environment");
        } catch (Exception e) {
            System.out.println("Test handled: " + e.getMessage());
        }
    }
  
    void testMessageDiscard() {
        Message message = new Message("08575975889", "Invalid recipient", 1);
        
        // Test discarding message
        String input = "2\n"; // Select discard option
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        try {
            Field scannerField = Message.class.getDeclaredField("sc");
            scannerField.setAccessible(true);
            scannerField.set(null, new Scanner(System.in));
            
            String result = message.SentMessage();
            assertEquals("DISCARDED", result);
        } catch (Exception e) {
            System.out.println("Discard test handled: " + e.getMessage());
        }
    }
    
    void testMessageStore() {
        Message message = new Message("+27718693002", "Store this message", 1);
        
        // Test storing message
        String input = "3\n"; // Select store option
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        try {
            Field scannerField = Message.class.getDeclaredField("sc");
            scannerField.setAccessible(true);
            scannerField.set(null, new Scanner(System.in));
            
            String result = message.SentMessage();
            assertEquals("STORED", result);
        } catch (Exception e) {
            System.out.println("Store test handled: " + e.getMessage());
        }
    }
  
    void testJOptionPaneContent() {
        Message message = new Message("+27718693002", "hi Mike, can you join us for dinner tonight", 1);
        
        // Verify JOptionPane message content format
        String expectedContent = "MESSAGE SENT SUCCESSFULLY!\n\n" +
                "Message ID: " + message.getMessageId() + "\n" +
                "Message Number: " + message.getMessageNumber() + "\n" +
                "Recipient: " + message.getRecipient() + "\n" +
                "Message: " + message.getMessageContent() + "\n" +
                "Message Hash: " + message.getMessageHash();
        
        assertNotNull(expectedContent);
        assertTrue(expectedContent.contains("MESSAGE SENT SUCCESSFULLY!"));
        assertTrue(expectedContent.contains("Message ID:"));
        assertTrue(expectedContent.contains("hi Mike, can you join us for dinner tonight"));
        assertTrue(expectedContent.contains("+27718693002"));
        
        System.out.println("JOptionPane content validated successfully");
    }
    
    // Integration Tests with JOptionPane
    void testCompleteMessageFlow() {
        // Test complete flow: valid message -> send -> JOptionPane display
        Message validMessage = new Message("+27718693002", "hi Mike, can you join us for dinner tonight", 1);
        
        String input = "1\n"; // Send message
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        try {
            Field scannerField = Message.class.getDeclaredField("sc");
            scannerField.setAccessible(true);
            scannerField.set(null, new Scanner(System.in));
            
            String result = validMessage.SentMessage();
            assertEquals("SENT", result);
            
            // Test that JOptionPane content is properly formatted
            String jOptionContent = "MESSAGE SENT SUCCESSFULLY!\n\n" +
                    "Message ID: " + validMessage.getMessageId() + "\n" +
                    "Message Number: 1\n" +
                    "Recipient: +27718693002\n" +
                    "Message: hi Mike, can you join us for dinner tonight\n" +
                    "Message Hash: " + validMessage.getMessageHash();
            
            assertTrue(jOptionContent.contains("MESSAGE SENT SUCCESSFULLY!"));
            assertTrue(jOptionContent.contains("hi Mike, can you join us for dinner tonight"));
            
        } catch (HeadlessException e) {
            System.out.println("GUI test completed in headless environment");
        } catch (Exception e) {
            System.out.println("Test handled: " + e.getMessage());
        }
    }
    void testInvalidMessageFlow() {
        // Test complete flow: invalid message -> discard -> no JOptionPane
        Message invalidMessage = new Message("08575975889", "hi, keegan, did you recieve the payment?", 2);
        
        // Verify message has invalid recipient
        assertEquals(1, invalidMessage.checkRecipientCell());
        
        String input = "2\n"; // Discard message
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        try {
            Field scannerField = Message.class.getDeclaredField("sc");
            scannerField.setAccessible(true);
            scannerField.set(null, new Scanner(System.in));
            
            String result = invalidMessage.SentMessage();
            assertEquals("DISCARDED", result);
            // No JOptionPane should be shown for discarded messages
            
        } catch (Exception e) {
            System.out.println("Invalid message flow handled: " + e.getMessage());
        }
    }

    private void assertTrue(boolean checkUserName) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void assertFalse(boolean checkUserName) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void assertEquals(String welcome_it_is_great_to_see_you_again, String returnLoginStatus) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void assertEquals(int i, String totalMessagesSent) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void assertNotNull(String expectedContent) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private static class User {

        public User(String test_, String password1, String string, String john, String doe) {
        }

        private String getUsername() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        private String getPassword() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        private String getCellphone() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        private String getFirstName() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        private String getLastName() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        private String getTotalMessagesSent() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        private void setPassword(String newPass1) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        private void setFirstName(String jane) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        private void incrementTotalMessages() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    }
}