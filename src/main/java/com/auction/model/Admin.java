package com.auction.model;

/**
 * Lớp Admin đại diện cho quản trị viên trong hệ thống.
 * Kế thừa từ lớp trừu tượng User.
 */
public class Admin extends User {
    // Admin có thể có thêm các thuộc tính đặc thù, ví dụ như cấp độ quyền hạn
    private int accessLevel;

    public Admin(int id, String username, String password, String fullName, String email, int accessLevel) {
        // Gọi constructor của lớp cha User
        super(id, username, password, fullName, email);
        this.accessLevel = accessLevel;
    }

    // Getter và Setter cho thuộc tính riêng
    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    // Một phương thức ví dụ thể hiện quyền quản lý hệ thống
    public void banUser(User user) {
        System.out.println("Admin " + this.getUsername() + " đã khóa tài khoản: " + user.getUsername());
    }


    @Override
    public void displayRoleInfo() {
        System.out.println("Vai trò: Quản trị viên (Admin)");
        System.out.println("Cấp độ truy cập: " + accessLevel);
    }
}