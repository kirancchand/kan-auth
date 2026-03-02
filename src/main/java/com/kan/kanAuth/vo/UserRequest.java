package com.kan.kanAuth.vo;

public class UserRequest {
	private String username;
	private String email;
	private String password;
	private String firstName;
	private String lastName;
    private String middleName;
    private String mobno;
    private String dateofbirth;
    private ValueLabel gender;
    private ValueLabel status;
    private ValueLabel usertype;
    private ValueLabel branch;
    
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	public String getMobno() {
		return mobno;
	}
	public void setMobno(String mobno) {
		this.mobno = mobno;
	}
	public String getDateofbirth() {
		return dateofbirth;
	}
	public void setDateofbirth(String dateofbirth) {
		this.dateofbirth = dateofbirth;
	}
	public ValueLabel getGender() {
		return gender;
	}
	public void setGender(ValueLabel gender) {
		this.gender = gender;
	}
	public ValueLabel getStatus() {
		return status;
	}
	public void setStatus(ValueLabel status) {
		this.status = status;
	}
	public ValueLabel getUsertype() {
		return usertype;
	}
	public void setUsertype(ValueLabel usertype) {
		this.usertype = usertype;
	}
	public ValueLabel getBranch() {
		return branch;
	}
	public void setBranch(ValueLabel branch) {
		this.branch = branch;
	}
}
