package comp5216.sydney.edu.au.groupproject;

import java.io.File;

public class UploadTask {
    private File uploadFile;
    private String username;


    public void setUploadFile(File uploadFile) {
        this.uploadFile = uploadFile;
    }

    public File getUploadFile() {
        return uploadFile;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
