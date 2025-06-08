package com.clever.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.clever.dao.ContactRepository;
import com.clever.dao.UserRepository;
import com.clever.entities.Contact;
import com.clever.entities.User;
import com.clever.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	// method for adding common data to response

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {

		String userName = principal.getName();
		System.out.println("USERNAME" + userName);

		// get the user using username(Email)

		User user = userRepository.getUserbyUserName(userName);

		System.out.println("USER " + user);

		model.addAttribute("user", user);

	}

	// Dashboard home

	@GetMapping("/index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User Dashboard");
		String userName = principal.getName();
		return "normal/user_dashboard";
	}

	// Open add form handler

	@GetMapping("/add_contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// processing add form

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {
			String name = principal.getName();
			User user = this.userRepository.getUserbyUserName(name);

			// processing and uploading file....

			if (file.isEmpty()) {
				// if the file is empty then try our message
				System.out.println("file is empty");
				contact.setImage("contact.png");

			} else {
				// upload the file to folder and update the name to conatct
				contact.setImage(file.getOriginalFilename());

				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("image is uploaded");
			}

			contact.setUser(user);

			user.getContacts().add(contact);

			this.userRepository.save(user);

			System.out.println("DATA " + contact);

			System.out.println("Added to database");

			// Message success.......
			session.setAttribute("message", new Message("Your conatct is added !! Add more..", "success"));

		} catch (Exception e) {
			System.out.println("ERROR " + e.getMessage());
			e.printStackTrace();
			// message error
			session.setAttribute("message", new Message("Something went wrong !! Try again..", "danger"));
		}
		return "normal/add_contact_form";
	}

	// Show contact handler...
	// per page = 5[n]
	// current page= 0[page]

	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		m.addAttribute("title", "Show User Contacts");

		// send the contacts list from the database...

		String userName = principal.getName();
		User user = this.userRepository.getUserbyUserName(userName);

		// 1.currentPage-page
		// 2.Contact Per page -5
		Pageable pageable = PageRequest.of(page, 5);

		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(), pageable);

		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}

	// Showing specific contact details...

	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		//
		String userName = principal.getName();
		User user = this.userRepository.getUserbyUserName(userName);

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		return "normal/contact_detail";
	}

	// Delete Contact Handler..

	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session,
			Principal principal) {
		System.out.println("CID " + cId);

		Contact contact = this.contactRepository.findById(cId).get();

		// check.. Assingment remove the image

		// delete old photo

		User user = this.userRepository.getUserbyUserName(principal.getName());

		user.getContacts().remove(contact);

		this.userRepository.save(user);

		System.out.println("DELETED");
		session.setAttribute("message", new Message("Contact deleted succesfully...", "success"));
		return "redirect:/user/show-contacts/0";
	}

	// Open update form Handler...

	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model m) {
		m.addAttribute("title", "Update Contact");

		Contact contact = this.contactRepository.findById(cid).get();

		m.addAttribute("contact", contact);

		return "normal/update_form";
	}

	// Update contact handler..

	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model m, HttpSession session, Principal principal) {

		try {

			// fetch old contact detail for old photo delete

			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();

			// image
			if (!file.isEmpty()) {

				// delete old photo

				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldContactDetail.getImage());
				file1.delete();

				// update new photo

				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImage(file.getOriginalFilename());

			} else {
				contact.setImage(oldContactDetail.getImage());
			}

			User user = this.userRepository.getUserbyUserName(principal.getName());

			contact.setUser(user);

			this.contactRepository.save(contact);

			session.setAttribute("message", new Message("Your contact is updated", "success"));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "redirect:/user/" + contact.getcId() + "/contact";
	}

	// Your Profile Handler...

	@GetMapping("/profile")
	public String profileHandler(Model model, Principal principal, HttpSession session) {
		String userName = principal.getName();
		User user = this.userRepository.getUserbyUserName(userName);
		if (user.getValidated() == 0) {
			session.setAttribute("validation", "fail");
		}
//		User user = this.userRepository.getUserByUserName(principal.getName());
//		model.addAttribute("user", user);
		model.addAttribute("title", "User Profile");
//		System.out.println(user);
		return "normal/profile";
	}

	@GetMapping("/update-profile")
	public String updateProfileHandler(Model model) {
		model.addAttribute("title", "Edit Profile");
		return "normal/update_profile_form";
	}

	@PostMapping("/process-update-profile")
	public String processEditProfileForm(@RequestParam("about") String about,
			@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session)
			throws IOException {

		System.out.println(about);
		if (!file.isEmpty()) {
			System.out.println(file.getOriginalFilename());
		}

		String userName = principal.getName();
		User user = this.userRepository.getUserbyUserName(userName);

		user.setAbout(about);

		if (!file.isEmpty()) {

			if (file.getContentType().equals("image/jpeg") || file.getContentType().equals("image/png")
					|| file.getContentType().equals("image/jpg")) {

				user.setImageUrl(file.getOriginalFilename());

				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			}

		}

		this.userRepository.save(user);
		session.setAttribute("message", new Message("Profile updated successfully ...", "success"));
		return "normal/profile";
	}

	// open setting handler

	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}

	// change password handler..
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {
		System.out.println("OLDPASSWORD " + oldPassword);
		System.out.println("NEWPASSWORD " + newPassword);

		String userName = principal.getName();
		User currentUser = this.userRepository.getUserbyUserName(userName);

		if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {

			// change the password

			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your password is successfully changed...", "success"));

		} else {

			// error..
			session.setAttribute("message", new Message("Please Enter correct old password ", "danger"));
			return "redirect:/user/settings";

		}

		return "redirect:/user/index";
	}

}
