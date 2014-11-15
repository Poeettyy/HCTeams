package net.frozenorb.foxtrot.citadel;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import lombok.Getter;
import net.minecraft.util.org.apache.commons.io.FileUtils;
import org.bukkit.craftbukkit.libs.com.google.gson.GsonBuilder;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonParser;

import java.io.File;

/**
 * Created by macguy8 on 11/14/2014.
 */
public class CitadelHandler {

    @Getter private String capper;

    public CitadelHandler() {
        try {
            File citadelInfo = new File("citadelInfo.json");

            if (!citadelInfo.exists()) {
                citadelInfo.createNewFile();
                BasicDBObject dbo = new BasicDBObject();

                dbo.put("capper", "none");

                FileUtils.write(citadelInfo, new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(dbo.toString())));
            }

            BasicDBObject dbo = (BasicDBObject) JSON.parse(FileUtils.readFileToString(citadelInfo));

            if (dbo != null) {
                this.capper = dbo.getString("capper");

                if (this.capper.equalsIgnoreCase("none")) {
                    this.capper = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCapper(String capper) {
        this.capper = capper;

        try {
            File citadelInfo = new File("citadelInfo.json");
            BasicDBObject dbo = new BasicDBObject();

            dbo.put("capper", "none");

            citadelInfo.delete();
            FileUtils.write(citadelInfo, new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(dbo.toString())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}