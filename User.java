import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    public String userId;
    public String name;
    public String password;
    public String role;
    public String email;
    public String phoneNumber;

    // Constructor
    public User(String userId, String name, String password, String role, String email, String phoneNumber) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.role = role;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
    
    // Simple constructor for basic user
    public User(String userId, String name, String password, String role) {
        this(userId, name, password, role, "", "");
    }
    
    @Override
    public String toString() {
        return "User[" + userId + ": " + name + " (" + role + ")]";
    }
}
