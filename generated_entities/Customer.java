package com.gcu.chinook.generated;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Generated from table customer. */
@Entity
@Table(name = "customer")
class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @NotNull
    @Size(max = 40)
    @Column(name = "first_name", nullable = false, length = 40)
    private String firstName;

    @NotNull
    @Size(max = 20)
    @Column(name = "last_name", nullable = false, length = 20)
    private String lastName;

    @Size(max = 80)
    @Column(name = "company", length = 80)
    private String company;

    @Size(max = 70)
    @Column(name = "address", length = 70)
    private String address;

    @Size(max = 40)
    @Column(name = "city", length = 40)
    private String city;

    @Size(max = 40)
    @Column(name = "state", length = 40)
    private String state;

    @Size(max = 40)
    @Column(name = "country", length = 40)
    private String country;

    @Size(max = 10)
    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Size(max = 24)
    @Column(name = "phone", length = 24)
    private String phone;

    @Size(max = 24)
    @Column(name = "fax", length = 24)
    private String fax;

    @NotNull
    @Size(max = 60)
    @Column(name = "email", nullable = false, length = 60)
    private String email;

    @Column(name = "support_rep_id")
    private Integer supportRepId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_rep_id", insertable = false, updatable = false)
    private Employee supportRep;

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
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

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getSupportRepId() {
        return supportRepId;
    }

    public void setSupportRepId(Integer supportRepId) {
        this.supportRepId = supportRepId;
    }

    public Employee getSupportRep() {
        return supportRep;
    }
}
