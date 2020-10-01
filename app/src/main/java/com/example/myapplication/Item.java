package com.example.myapplication;

public class Item {
    String PhotoListName;
    int PhotoListImage;

    public Item(String PhotoName,int PhotoImage)
    {
        this.PhotoListImage=PhotoImage;
        this.PhotoListName=PhotoName;
    }
    public String getPhotoName()
    {
        return PhotoListName;
    }
    public int getPhotoImage()
    {
        return PhotoListImage;
    }
}
