package exchange.xyvox.Controllers;

import exchange.xyvox.Models.AppUser;
import exchange.xyvox.Models.AppUserRegisterDTO;
import exchange.xyvox.Models.Wallet;
import exchange.xyvox.Services.AppUserService;
import exchange.xyvox.Services.RoleService;
import exchange.xyvox.Services.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller()
@RequestMapping("/xyvox/api/v1/auth")
public class AuthController {

    private final AppUserService appUserService;
    private final RoleService roleService;

    private final PasswordEncoder delegatingPasswordEncoder;
    private final Map<String, PasswordEncoder> passwordEncodersMap;
    private final Random rand = new SecureRandom();
    private final List<String> encoders = new ArrayList<>();
    private final WalletService walletService;


    public AuthController(AppUserService appUserService, RoleService roleService, PasswordEncoder delegatingPasswordEncoder,
                          @Qualifier("customEncoders") Map<String, PasswordEncoder> passwordEncodersMap, WalletService walletService) {
        this.appUserService = appUserService;
        this.roleService = roleService;
        this.delegatingPasswordEncoder = delegatingPasswordEncoder;
        this.passwordEncodersMap = passwordEncodersMap;

        this.encoders.addAll(passwordEncodersMap.keySet());
        this.walletService = walletService;
    }

    @GetMapping("/main-page")
    public String mainPage(Model model) {
        return "main-page";
    }

    @GetMapping
    public String authForm(
            @RequestParam(value = "form", defaultValue = "login") String form,
            Model model
    ) {
        model.addAttribute("userDto", new AppUserRegisterDTO());
        model.addAttribute("activeForm", form);
        return "auth"; // имя Thymeleaf-шаблона
    }


    @PostMapping("/login")
    public String loginProcessing() {
        return "redirect:/auth?form=login";
    }


    @PostMapping("/register")
    public String registerProcessing(
            @Valid @ModelAttribute("userDto") AppUserRegisterDTO userDto,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("activeForm", "signup");
            return "auth";
        }

        if (appUserService.getUserByEmail(userDto.getEmail()) != null) {
            model.addAttribute("emailExistsError", "Account with this email already registered");
            model.addAttribute("activeForm", "signup");
            return "auth";
        }

        if (appUserService.getUserByUsername(userDto.getUsername()) != null) {
            model.addAttribute("usernameExistsError", "Username is already taken");
            model.addAttribute("activeForm", "signup");
            return "auth";
        }

        if (encoders.isEmpty()) {
            throw new IllegalStateException("No custom encoders available!");
        }
        int idx = rand.nextInt(encoders.size());
        String chosenId = encoders.get(idx);
        PasswordEncoder specificEncoder = passwordEncodersMap.get(chosenId);
        if (specificEncoder == null) {
            throw new IllegalStateException("Encoder not found for id: " + chosenId);
        }
        String hashed = specificEncoder.encode(userDto.getPassword());
        String passwordWithPrefix = "{" + chosenId + "}" + hashed;

        AppUser user = new AppUser();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordWithPrefix);
        user.setEnabled(true);
        user.setRole(roleService.getRoleByName("ROLE_USER"));
        Wallet wallet = new Wallet();
        user.setWallet(wallet);
        wallet.setOwner(user);

        appUserService.addNewUser(user);

        return "redirect:/auth?form=login&registered";
    }




}
