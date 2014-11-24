package net.frozenorb.foxtrot.koth;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import com.mysql.jdbc.StringUtils;
import lombok.Getter;
import net.frozenorb.foxtrot.FoxtrotPlugin;
import net.frozenorb.foxtrot.command.CommandHandler;
import net.frozenorb.foxtrot.command.objects.ParamTabCompleter;
import net.frozenorb.foxtrot.command.objects.ParamTransformer;
import net.frozenorb.foxtrot.koth.tasks.KOTHSchedulerTask;
import net.minecraft.util.com.google.gson.Gson;
import net.minecraft.util.org.apache.commons.io.FileUtils;
import net.minecraft.util.org.apache.commons.io.IOUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.com.google.gson.GsonBuilder;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonParser;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by macguy8 on 10/31/2014.
 */
public class KOTHHandler {

    @Getter private static Set<KOTH> KOTHs = new HashSet<KOTH>();
    @Getter private static Map<Integer, String> kothSchedule = new HashMap<Integer, String>();

    public static void init() {
        loadKOTHs();
        loadSchedules();

        Calendar date = Calendar.getInstance();

        date.set(Calendar.MINUTE, 60);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        (new Timer("KOTH Scheduler")).schedule(new KOTHSchedulerTask(), date.getTime(), TimeUnit.HOURS.toMillis(1));

        CommandHandler.registerTransformer(KOTH.class, new ParamTransformer() {

            @Override
            public Object transform(CommandSender sender, String source) {
                KOTH koth = getKOTH(source);

                if (koth == null) {
                    sender.sendMessage(ChatColor.RED + "No KOTH with the name " + source + " found.");
                    return (null);
                }

                return (koth);
            }

        });

        CommandHandler.registerTabCompleter(KOTH.class, new ParamTabCompleter() {

            public List<String> tabComplete(Player sender, String source) {
                List<String> completions = new ArrayList<String>();

                for (KOTH koth : getKOTHs()) {
                    if (StringUtils.startsWithIgnoreCase(koth.getName(), source)) {
                        completions.add(koth.getName());
                    }
                }

                return (completions);
            }

        });

        new BukkitRunnable() {

            public void run() {
                for (KOTH koth : KOTHs) {
                    if (koth.isActive()) {
                        koth.tick();
                    }
                }
            }

        }.runTaskTimer(FoxtrotPlugin.getInstance(), 5L, 5L);
    }

    public static void loadKOTHs() {
        try {
            File kothsBase = new File("KOTHs");

            if (!kothsBase.exists()) {
                kothsBase.mkdir();
            }

            for (File kothFile : kothsBase.listFiles()) {
                if (kothFile.getName().endsWith(".json")) {
                    BufferedInputStream e = new BufferedInputStream(new FileInputStream(kothFile));

                    StringWriter writer = new StringWriter();
                    IOUtils.copy(e, writer, "utf-8");

                    KOTHs.add((new Gson()).fromJson(writer.toString(), KOTH.class));

                    e.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadSchedules() {
        kothSchedule.clear();

        try {
            File kothSchedule = new File("kothSchedule.json");

            if (!kothSchedule.exists()) {
                kothSchedule.createNewFile();
                FileUtils.write(kothSchedule, new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(new BasicDBObject().toString())));
            }

            BasicDBObject dbo = (BasicDBObject) JSON.parse(FileUtils.readFileToString(kothSchedule));

            if (dbo != null) {
                for (Map.Entry<String, Object> entry : dbo.entrySet()) {
                    KOTHHandler.kothSchedule.put(Integer.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveKOTHs() {
        try {
            File kothsBase = new File("KOTHs");

            if (!kothsBase.exists()) {
                kothsBase.mkdir();
            }

            for (File kothFile : kothsBase.listFiles()) {
                kothFile.delete();
            }

            for (KOTH koth : KOTHs) {
                File kothFile = new File(kothsBase, koth.getName() + ".json");
                FileWriter e = new FileWriter(kothFile);

                e.write((new Gson()).toJson(koth));

                e.flush();
                e.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static KOTH getKOTH(String name) {
        for (KOTH koth : KOTHs) {
            if (koth.getName().equalsIgnoreCase(name)) {
                return (koth);
            }
        }

        return (null);
    }

    public static void onSchedulerTick() {
        for (Player player : FoxtrotPlugin.getInstance().getServer().getOnlinePlayers()) {
            if (player.isOp()) {
                player.sendMessage(ChatColor.DARK_AQUA + "Running KOTH scheduler task...");
            }
        }

        // Don't start a KOTH if another one is active.
        for (KOTH koth : KOTHHandler.getKOTHs()) {
            if (koth.isActive()) {
                return;
            }
        }

        Calendar date = Calendar.getInstance();
        int hour = date.get(Calendar.HOUR_OF_DAY);

        if (kothSchedule.containsKey(hour)) {
            KOTH koth = getKOTH(kothSchedule.get(hour));

            if (koth == null) {
                FoxtrotPlugin.getInstance().getLogger().warning("The KOTH Scheduler has a schedule for a KOTH named " + kothSchedule.get(hour) + ", but the KOTH does not exist.");
                return;
            }

            koth.activate();
        }
    }

}