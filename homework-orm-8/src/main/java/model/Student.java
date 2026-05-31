package model;

import orm.annotation.*;
import java.time.LocalDate;

@Table(name = "students")
public class Student {
    @Id
    private Long id;

    @Column
    private String name;

    @Column
    private int age;

    @Column
    private double grade;

    @Column
    private LocalDate enrollmentDate;  // Новое поле для LocalDate

    public Student() {}

    public Student(String name, int age, double grade, LocalDate enrollmentDate) {
        this.name = name;
        this.age = age;
        this.grade = grade;
        this.enrollmentDate = enrollmentDate;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getGrade() { return grade; }
    public void setGrade(double grade) { this.grade = grade; }

    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "', age=" + age +
                ", grade=" + grade + ", enrollmentDate=" + enrollmentDate + "}";
    }
}