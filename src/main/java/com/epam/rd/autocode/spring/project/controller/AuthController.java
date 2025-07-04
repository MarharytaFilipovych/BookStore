package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.dto.request.ForgotPasswordDTO;
import com.epam.rd.autocode.spring.project.dto.request.LoginDTO;
import com.epam.rd.autocode.spring.project.dto.request.LogoutDTO;
import com.epam.rd.autocode.spring.project.dto.request.ResetPasswordDto;
import com.epam.rd.autocode.spring.project.dto.request.RefreshTokenDTO;
import com.epam.rd.autocode.spring.project.dto.TokenResponseDTO;
import com.epam.rd.autocode.spring.project.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("auth")
public class AuthController {

    private final AuthService authService;
    private final EmployeeService employeeService;
    private final ClientService clientService;

    public AuthController(AuthService authService,
                          EmployeeService employeeService,
                          ClientService clientService) {
        this.authService = authService;
        this.employeeService = employeeService;
        this.clientService = clientService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginDTO request){
        return ResponseEntity.ok(authService.loginUser(request));
    }

    @PostMapping("/register/employee")
    public ResponseEntity<String> register(@Valid @RequestBody EmployeeDTO employee){
        employeeService.addEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully registered!");
    }

    @PostMapping("/register/client")
    public ResponseEntity<String> register(@Valid @RequestBody ClientDTO client){
        clientService.addClient(client);
        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully registered!");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponseDTO> refreshToken(@NotNull @RequestBody @Valid RefreshTokenDTO request){
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody @Valid ForgotPasswordDTO request){
        authService.forgotPassword(request);
        return ResponseEntity.ok("The reset code was sent to the email " + request.getEmail());
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ResetPasswordDto request){
        authService.changePassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutDTO request){
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }
}
