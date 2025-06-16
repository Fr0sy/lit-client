package com.level.hub.launcher.data;

public class FilesData {

    private String name;
    private long size;
    private String hash;
    private String path;
    private String url;

    public FilesData(String name, long size, String hash, String path, String url) {
        this.name = name;
        this.size = size;
        this.hash = hash;
        this.path = path;
        this.url = url;
    }

    public String getName()
    {
        return name;
    }

    public long getSize()
    {
        return size;
    }
    public  String getHash()
    {
        return hash;
    }

    public  String getPath()
    {
        return path;
    }

    public  String getUrl()
    {
        return url;
    }

}
