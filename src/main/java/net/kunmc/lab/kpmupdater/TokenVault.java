package net.kunmc.lab.kpmupdater;

import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TokenVault
{
    public boolean success;
    private String token;

    public TokenVault()
    {
        this.token = "";

        if (!new File(new File("").getAbsolutePath(), "kpm.vault").exists())
            return;
        try
        {
            this.token = FileUtils.readFileToString(new File(new File("").getAbsolutePath(), "kpm.vault"), StandardCharsets.UTF_8);
            this.success = true;
        }
        catch (IOException e)
        {
            System.out.println("TOKENの読み込みに失敗しました。");
            this.success = false;
        }
    }

    public String getToken()
    {
        return this.token;
    }
}
