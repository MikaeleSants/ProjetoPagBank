package com.criando.projeto.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


//mapeando a entidade:
@Entity
//User é uma palavra reservada do banco h2, por isso tem que renomear o banco:
@Table(name = "tb_user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    //identificado o ID como chave primaria do banco de dados: @Id
    //como é uma chave numerica, ela vai ser autoimplementável lá no banco de dados
    //para informar isso usamos: @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Size(min = 3, max = 8, message = "A senha deve ter entre 3 e 8 caracteres.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "A senha deve conter pelo menos uma letra, um número e um símbolo especial (@$!%*?&)."
    )
    private String password;
    /* a relação entre user e order é de one to many, o que signifca que
     * será um user para varios pedidos, para indicar esse tipo de relação como chave estrageira
     * para o JPA, se usa a annotation @OneToMany(mappedBy: "o nome de como ele foi declarado")
     * o JsonIgnora é pra que não um loop na ligação entre user e order, pra um n ficar chamando o outro eternamente*/
    @JsonIgnore
    @OneToMany(mappedBy = "client")
    private List<Order> orders = new ArrayList<>();

    public User() {
    }

    public User(Long id, String name, String email, String phone, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
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

    public List<Order> getOrders() {
        return orders;
    }

}
