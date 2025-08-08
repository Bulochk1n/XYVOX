package exchange.xyvox.Models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AppUserRegisterDTO {

    @NotBlank(message = "Email is required")
    @Size(min = 5, max = 100, message = "Size is between 5 and 100")
    @Email(message = "Email is invalid")
    private String email;
    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 50, message = "Size is between 2 and 50")
    private String username;
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 12, message = "Size is between 8 and 12")
    private String password;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
