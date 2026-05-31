package model;

import orm.annotation.*;
import orm.annotation.validation.*;

@Table(name = "registration_forms")
public class RegistrationForm {

    @Id
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Логин не может быть пустым")
    @Size(min = 3, max = 30, message = "Логин должен быть от 3 до 30 символов")
    private String login;

    @Column(nullable = false)
    @NotNull(message = "Пароль не может быть null")
    @Size(min = 8, max = 100, message = "Пароль должен быть от 8 до 100 символов")
    private String password;

    @Column
    @Min(value = 18, message = "Возраст должен быть не меньше 18 лет")
    @Max(value = 120, message = "Возраст должен быть не больше 120 лет")
    private int age;

    // Конструкторы
    public RegistrationForm() {}

    public RegistrationForm(String login, String password, int age) {
        this.login = login;
        this.password = password;
        this.age = age;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}