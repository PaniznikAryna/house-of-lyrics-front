package net.houseoflyrics.dto;

public class RegistrationData {
    private Long id;  // Добавляем поле ID пользователя
    private String nickname;
    private String mail;
    private String password;
    private String profilePicture;
    private boolean admin;
    private int musicalInstrumentId; // ID выбранного инструмента

    // Геттеры и сеттеры:
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMail() {
        return mail;
    }
    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfilePicture() {
        return profilePicture;
    }
    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public boolean isAdmin() {
        return admin;
    }
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public int getMusicalInstrumentId() {
        return musicalInstrumentId;
    }
    public void setMusicalInstrumentId(int musicalInstrumentId) {
        this.musicalInstrumentId = musicalInstrumentId;
    }
}
