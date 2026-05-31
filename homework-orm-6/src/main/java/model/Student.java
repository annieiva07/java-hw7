package model; 
 
import orm.annotation.*; 
 
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
 
    public Student() {} 
 
    public Student(String name, int age, double grade) { 
        this.name = name; 
        this.age = age; 
        this.grade = grade; 
    } 
 
    public Long getId() { return id; } 
    public void setId(Long id) { this.id = id; } 
    public String getName() { return name; } 
    public void setName(String name) { this.name = name; } 
    public int getAge() { return age; } 
    public void setAge(int age) { this.age = age; } 
    public double getGrade() { return grade; } 
    public void setGrade(double grade) { this.grade = grade; } 
} 
