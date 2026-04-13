package com.dynademo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dynademo.csvdb.CsvTableStore;
import com.dynademo.dto.Skill;
import com.dynademo.dto.Student;
import com.dynademo.dto.User;
import com.google.gson.Gson;

@Controller
@RequestMapping("/csv")
public class CsvController {

	@GetMapping("/insertCsv")
	@ResponseBody
	public Map<String, String> insertCsv() {
		Map<String, String> ret = new HashMap<>();
		
        CsvTableStore store = new CsvTableStore();
        
		Student student = new Student();
		student.setName("Taro Yamada");
		student.setAge(17);
		student.setSchool("Tokyo Highschool");
		student.setGrade("2");
		student.setRoom("14");
		Skill skill = new Skill();
		skill.setName("base ball");
		skill.setLevel(2);		
		List<Skill> skillList = new ArrayList<>();
		skillList.add(skill);
		student.setSkillList(skillList);

        // insert
        store.insert("user", student);
        
		Gson gson = new Gson();
		ret.put("user", gson.toJson(student));
        return ret;
	}
	
	@GetMapping("/updateCsv")
	@ResponseBody
	public Map<String, String> updateCsv() {
		Map<String, String> ret = new HashMap<>();
		
        CsvTableStore store = new CsvTableStore();
        
		User user = store.select("user", User.class);
		for (int i = 0; i < user.getSkillList().size(); i++) {
			Skill skill = user.getSkillList().get(i);
			skill.setName("football");
			skill.setLevel(3);
		}

        // insert
        store.update("user", user);
        
		Gson gson = new Gson();
		ret.put("user", gson.toJson(user));
        return ret;
	}
	
	@GetMapping("/getUserFromCsv")
	@ResponseBody
	public Map<String, String> getUserFromCsv() {
		Map<String, String> ret = new HashMap<>();
		
        CsvTableStore store = new CsvTableStore();
        
		Student student = store.select("user", Student.class);
        
		Gson gson = new Gson();
		ret.put("student", gson.toJson(student));
        return ret;
	}
	
}
