package com.clever.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.clever.entities.Contact;
import com.clever.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
	// pagination...

	@Query("from Contact as c where c.user.id =:userId")

	// Pageable has two variable..
	// 1.currentPage-page
	// 2.Contact Per page -5

	public Page<Contact> findContactByUser(@Param("userId") int userId, Pageable pageable);
	
	//search contact
	public List<Contact>findByNameContainingAndUser(String name,User user);
}
