package exchange.xyvox.Models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AppUserUpdateDTO {
    @NotBlank @Size(min = 3, max = 50)
    private String username;

    @Email @NotBlank
    private String email;

    @Pattern(regexp = "^$|.{8,}", message = "Password must be at least 8 characters (or left blank to keep current)"
    )
    private String password;

    public AppUserUpdateDTO() {}

    public AppUserUpdateDTO(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}