package comp5216.sydney.edu.au.groupproject;

import java.io.File;
import java.util.List;

public class Type {
    private String name;
    private List<File> clothes;

    public Type(String name, List<File> clothes) {
        this.name = name;
        this.clothes = clothes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<File> getClothes() {
        return clothes;
    }

    public void setClothes(List<File> clothes) {
        this.clothes = clothes;
    }
}
