package exchange.xyvox.Controllers;


import exchange.xyvox.Authentication.CustomUserDetailsService;
import exchange.xyvox.Models.AppUser;
import exchange.xyvox.Models.AppUserUpdateDTO;
import exchange.xyvox.Models.Wallet;
import exchange.xyvox.Models.WalletCoin;
import exchange.xyvox.Services.AppUserService;
import exchange.xyvox.Services.WalletService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/xyvox/api/v1/users")
public class UsersController {

    private final AppUserService appUserService;
    private final CustomUserDetailsService customUserDetailsService;
    private final WalletService walletService;

    public UsersController(AppUserService appUserService, CustomUserDetailsService customUserDetailsService, WalletService walletService) {
        this.appUserService = appUserService;
        this.customUserDetailsService = customUserDetailsService;
        this.walletService = walletService;
    }




    @GetMapping("/my-profile")
    public String myProfile(
            Authentication authentication,
            @RequestParam(value = "edit", required = false, defaultValue = "false") boolean edit,
            Model model
    ) {
        AppUser user = appUserService.getUserByEmail(authentication.getName());

        Wallet wallet = user.getWallet();
        Set<WalletCoin> balances = wallet.getWalletCoins();
        List<WalletCoin> balances_sorted = new ArrayList<WalletCoin>(balances);
        balances_sorted.sort(Comparator.comparing(WalletCoin::getCoinvalue).reversed());




        model.addAttribute("balances", balances_sorted);
        model.addAttribute("user", user);
        model.addAttribute("editMode", edit);
        model.addAttribute("editDto", new AppUserUpdateDTO(user.getUsername(), user.getEmail(), ""));
        return "my-profile";
    }

    @PostMapping("/my-profile")
    public String updateProfile(
            Authentication authentication,
            @Valid @ModelAttribute("editDto") AppUserUpdateDTO dto,
            BindingResult errors,
            Model model
    ) {
        if (errors.hasErrors()) {
            model.addAttribute("user", appUserService.getUserByEmail(authentication.getName()));
            model.addAttribute("editMode", true);
            return "my-profile";
        }

        appUserService.updateProfile(
                authentication.getName(),
                dto.getUsername(),
                dto.getEmail(),
                dto.getPassword()
        );

        String newEmail = dto.getEmail();
        String oldEmail = authentication.getName();
        if (!oldEmail.equals(newEmail)) {
            UserDetails updatedUserDetails =
                    ((CustomUserDetailsService) customUserDetailsService)
                            .loadUserByUsername(newEmail);

            UsernamePasswordAuthenticationToken newAuth =
                    new UsernamePasswordAuthenticationToken(
                            updatedUserDetails,
                            updatedUserDetails.getPassword(),
                            updatedUserDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }

        return "redirect:/xyvox/api/v1/users/my-profile";
    }

}

