package com.project.belt.controllers;

import java.security.Principal;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.belt.models.User;
import com.project.belt.services.UserService;
import com.project.belt.validators.UserValidator;

@Controller
public class Users {
	
	private UserService us;
	private UserValidator uv;
	public Users (UserService us, UserValidator uv) {
		this.us = us;
		this.uv = uv;
	}
	
	@RequestMapping("/")
	public String landing(Model m,
		@Valid @ModelAttribute("user") User u, Principal p,
		@RequestParam(value="error", required=false) String e,
		@RequestParam(value="logout", required=false) String l) {
		
		if (p != null) { return "redirect:/dashboard"; }
		if (e != null) { m.addAttribute("error", "Invalid credentials."); }
		if (l != null) { m.addAttribute("logout", "Thanks for visiting!"); }
		return "landing";
	}
	
	@PostMapping("/register")
	public String register(@Valid @ModelAttribute("user") User u,
		BindingResult r, Model m, RedirectAttributes f) {
		
		uv.validate(u, r);
		if (r.hasErrors()) { m.addAttribute("errors", "!"); return "landing"; }
		if (us.getByLevel(3).isEmpty()) { us.createSuper(u); }
		else { us.createUser(u); } f.addFlashAttribute("thanks", "Thanks for signing up!");
		return "redirect:/";
	}
	
	@RequestMapping("/dashboard")
	public String dash(Principal p, Model m) {
		User user = us.getByUsername(p.getName());
		us.updateLastLogin(user);
		if (user.getLevel() == 3) { return "redirect:/admin"; }
		else { m.addAttribute("user", user); return "dash"; }
	}
	
	@RequestMapping("/admin")
	public String admin(Principal p, Model m) {
		m.addAttribute("user", us.getByUsername(p.getName()));
		m.addAttribute("users", us.getAll());
		return "admin";
	}
	
	@RequestMapping("/admin/user{u}/promote")
	public String promote(@PathVariable("u") Long u) {
		us.promote(us.get(u));
		return "redirect:/admin";
	}
	
	@RequestMapping("/admin/user{u}/demote")
	public String demote(@PathVariable("u") Long u) {
		us.demote(us.get(u));
		return "redirect:/admin";
	}
	
	@RequestMapping("/admin/user{u}/delete")
	public String delete(@PathVariable("u") Long u) {
		us.delete(us.get(u));
		return "redirect:/admin";
	}
	
}
