package net.kunmc.lab.kpmupdater.utils;

public class InstallResult
{

    public String fileName;
    public String pluginName;
    public int add;
    public int remove;
    public int modify;
    public boolean success;

    public InstallResult(int add, int remove, int modify, boolean success)
    {
        this("", "", add, remove, modify, success);
    }

    public InstallResult(String fileName, String pluginName, int add, int remove, int modify, boolean success)
    {
        this.fileName = fileName;
        this.pluginName = pluginName;
        this.add = add;
        this.remove = remove;
        this.modify = modify;
        this.success = success;
    }
}
