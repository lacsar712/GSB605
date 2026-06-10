package com.vacation.student.controller;

import com.vacation.student.dto.Result;
import com.vacation.student.service.SchoolNameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schools")
public class SchoolNameController {
    
    @Autowired
    private SchoolNameService schoolNameService;
    
    @GetMapping("/suggest")
    public Result<List<String>> suggest(@RequestParam String keyword) {
        List<String> suggestions = schoolNameService.suggest(keyword);
        return Result.success(suggestions);
    }
}
