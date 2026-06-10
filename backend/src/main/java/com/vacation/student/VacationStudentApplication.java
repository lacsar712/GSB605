package com.vacation.student;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.vacation.student.mapper")
public class VacationStudentApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(VacationStudentApplication.class, args);
    }
}
