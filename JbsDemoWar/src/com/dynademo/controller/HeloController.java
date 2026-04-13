package com.dynademo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dynademo.dto.Employee;
import com.dynademo.dto.Skill;
import com.dynademo.dto.Student;
import com.dynademo.dto.User;
import com.google.gson.Gson;

@Controller
@RequestMapping("/helo")
public class HeloController {

	@GetMapping("/index")
	public String index(HttpServletRequest request) {
		String root = request.getServletContext().getRealPath("/");
		if ("http://hoge/fuga/piyo".indexOf("/fuga") > -1) {
			System.out.println("indexof true!");
		}
		System.out.println("root is :" + root);
		return "helo/sample";
	}
	
	@GetMapping("/createStudent")
	@ResponseBody
	public Map<String, String> createSession(HttpServletRequest request) {
		Map<String, String> ret = new HashMap<>();
		HttpSession session = request.getSession(false);
		
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
		
		session.setAttribute("user", student);
		
		ret.put("result", "Success for create student.");
		return ret;
	}
	
	@GetMapping("/updateStudentSkill")
	@ResponseBody
	public Map<String, String> updateStudentSkill(HttpServletRequest request) {
		Map<String, String> ret = new HashMap<>();
		HttpSession session = request.getSession(false);
		
		User user = (User) session.getAttribute("user");
		for (int i = 0; i < user.getSkillList().size(); i++) {
			Skill skill = user.getSkillList().get(i);
			skill.setName("football");
			skill.setLevel(3);
		}
		
		Gson gson = new Gson();
		ret.put("user", gson.toJson(user));
		return ret;
	}
	

	@GetMapping("/getStudent")
	@ResponseBody
	public Map<String, String> getStudent(HttpServletRequest request) {
		Map<String, String> ret = new HashMap<>();
		HttpSession session = request.getSession(false);
		
		Student student = (Student) session.getAttribute("user");
		Gson gson = new Gson();
		ret.put("student", gson.toJson(student));
		return ret;
	}
	
	@GetMapping("/getEmployee")
	@ResponseBody
	public Map<String, String> getEmployee(HttpServletRequest request) {
		Map<String, String> ret = new HashMap<>();
		try {
			HttpSession session = request.getSession(false);
			
			Employee employee = (Employee) session.getAttribute("user");
			Gson gson = new Gson();
			ret.put("employee", gson.toJson(employee));			
		} catch (Exception e) {
			e.printStackTrace();
			ret.put("exception", e.toString());
		}
		return ret;
	}
	
	@GetMapping("/removeSession")
	@ResponseBody
	public Map<String, String> removeSession(HttpServletRequest request) {
		Map<String, String> ret = new HashMap<>();
		HttpSession session = request.getSession(false);
		session.invalidate();
		ret.put("result", "Removed session..");
		return ret;
	}
	
	
}
