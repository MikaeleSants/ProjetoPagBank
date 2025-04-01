package com.criando.projeto.entities;

import com.criando.projeto.entities.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Entity
@Table(name = "tb_user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "O nome não pode estar vazio.")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
    private String name;
    @NotBlank(message = "O e-mail não pode estar vazio.")
    @Email(message = "O e-mail deve ser válido.")
    @Column(unique = true)
    private String email;
    @NotBlank(message = "O telefone não pode estar vazio.")
    @Size(min = 9, max = 17, message = "O telefone deve ter entre 3 e 17 caracteres.")
    private String phone;
    @NotBlank(message = "A senha não pode estar vazia.")
    // validações de senha estão inclusas no service, para passar no spring security
    private String password;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @JsonIgnore
    @OneToMany(mappedBy = "client")
    private List<Order> orders = new ArrayList<>();

    public User() {
    }

    public User(Long id, String name, String email, String phone, String password, UserRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Order> getOrders() {
        return orders;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }



}
