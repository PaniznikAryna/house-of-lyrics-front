package net.houseoflyrics.dto;

public class UploadedCompositionDTO {
    private String file;      // Путь к аудиофайлу (например, "my_composition/my_song.mp3")
    private String title;     // Заголовок композиции
    private String picture;   // Название файла обложки (например, "cover.jpg")

    // Геттеры и сеттеры
    public String getFile() { return file; }
    public void setFile(String file) { this.file = file; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }
}
