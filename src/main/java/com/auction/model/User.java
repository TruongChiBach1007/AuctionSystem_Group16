package com.auction.model;

//Lớp trừu tượng đại diện cho người dùng trong hệ thống
//  Áp dụng tính trừu tượng (Abstraction) để định nghĩa khung cơ bản

public abstract class User {
    private int id;
    private String username;
    private String password; // Nên được băm (hash) trước khi lưu
    private String fullName;
    private String email;

    // Constructor cơ bản
    public User(int id, String username, String password, String fullName, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
    }

    // Getter và Setter (Tính đóng gói - Encapsulation)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /**
     * Phương thức trừu tượng để hiển thị thông tin đặc trưng của từng vai trò.
     * Thể hiện tính đa hình (Polymorphism) khi các lớp con triển khai lại[cite: 121].
     */
    public abstract void displayRoleInfo();
}