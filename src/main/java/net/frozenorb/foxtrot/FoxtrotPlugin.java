package net.frozenorb.foxtrot;

import com.comphenix.packetwrapper.WrapperPlayServerOpenSignEntity;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import lombok.Getter;
import net.frozenorb.Utilities.DataSystem.Regioning.RegionManager;
import net.frozenorb.foxtrot.armor.ClassHandler;
import net.frozenorb.foxtrot.armor.Kit;
import net.frozenorb.foxtrot.armor.KitManager;
import net.frozenorb.foxtrot.command.CommandRegistrar;
import net.frozenorb.foxtrot.command.subcommand.subcommands.teamsubcommands.Claim;
import net.frozenorb.foxtrot.command.subcommand.subcommands.teamsubcommands.Subclaim;
import net.frozenorb.foxtrot.diamond.MountainHandler;
import net.frozenorb.foxtrot.game.MinigameManager;
import net.frozenorb.foxtrot.game.games.koth.KOTHs;
import net.frozenorb.foxtrot.jedis.JedisCommand;
import net.frozenorb.foxtrot.jedis.RedisSaveTask;
import net.frozenorb.foxtrot.jedis.persist.*;
import net.frozenorb.foxtrot.listener.*;
import net.frozenorb.foxtrot.nametag.NametagManager;
import net.frozenorb.foxtrot.nms.EntityRegistrar;
import net.frozenorb.foxtrot.raid.DTRHandler;
import net.frozenorb.foxtrot.server.LocationTickStore;
import net.frozenorb.foxtrot.server.PacketBorder.BorderThread;
import net.frozenorb.foxtrot.server.ServerManager;
import net.frozenorb.foxtrot.team.TeamManager;
import net.frozenorb.foxtrot.team.claims.LandBoard;
import net.frozenorb.foxtrot.visual.BossBarManager;
import net.frozenorb.foxtrot.visual.scoreboard.ScoreboardManager;
import net.frozenorb.foxtrot.visual.TabHandler;
import net.frozenorb.mShared.Shared;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.jline.internal.InputStreamReader;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public class FoxtrotPlugin extends JavaPlugin {
	private static FoxtrotPlugin instance;

	/*
	 * ---- FIELDS ----
	 */
    public static final Random RANDOM = new Random();

	private JedisPool pool;

	@Getter private TeamManager teamManager;
	@Getter private ServerManager serverManager;
	@Getter private KitManager kitManager;

	@Getter private BossBarManager bossBarManager;
	@Getter private ScoreboardManager scoreboardManager;
	@Getter private MinigameManager minigameManager;

	@Getter private PlaytimeMap playtimeMap;
	@Getter private OppleMap oppleMap;
	@Getter private DeathbanMap deathbanMap;
	@Getter private JoinTimerMap joinTimerMap;
	@Getter private KillsMap killsMap;
    @Getter private ChatModeMap chatModeMap;
    @Getter private ToggleLightningMap toggleLightningMap;
    @Getter private FishingKitMap fishingKitMap;

	@Override
	public void onEnable() {
		try {
			EntityRegistrar.registerCustomEntities();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		Shared.get().getProfileManager().setNametagsEnabled(false);

		instance = this;
		pool = new JedisPool(new JedisPoolConfig(), "localhost");
		bossBarManager = new BossBarManager();
        KOTHs.init();

		RegionManager.register(this);
		RegionManager.get();

		LocationTickStore.getInstance().runTaskTimer(this, 1L, 1L);

		new DTRHandler().runTaskTimer(this, 20L, 20L * 60);
		new RedisSaveTask().runTaskTimer(this, 13200L, 13200L);

		ClassHandler chandler = new ClassHandler();

		chandler.runTaskTimer(this, 2L, 2L);
		Bukkit.getPluginManager().registerEvents(chandler, this);

		Bukkit.getScheduler().runTaskTimer(this, bossBarManager, 20L, 20L);
		Bukkit.getScheduler().runTaskTimer(this, new TabHandler(), 0, 10);

		new CommandRegistrar().register();

		teamManager = new TeamManager(this);
		LandBoard.getInstance().loadFromTeams();

		serverManager = new ServerManager();

		minigameManager = new MinigameManager();

		scoreboardManager = new ScoreboardManager();

		setupPersistence();

		new BorderThread().start();

		kitManager = new KitManager();
		kitManager.loadKits();

        Bukkit.getPluginManager().registerEvents(new KOTHListener(), this);
		Bukkit.getPluginManager().registerEvents(new EndListener(), this);
		Bukkit.getPluginManager().registerEvents(new BorderListener(), this);
		Bukkit.getPluginManager().registerEvents(new FoxListener(), this);
        Bukkit.getPluginManager().registerEvents(new RoadListener(), this);
		Bukkit.getPluginManager().registerEvents(new Subclaim("", ""), this);
		Bukkit.getPluginManager().registerEvents(new Claim("", ""), this);

		for (Player p : Bukkit.getOnlinePlayers()) {
			playtimeMap.playerJoined(p);
			NametagManager.sendPacketsInitialize(p);

			NametagManager.reloadPlayer(p);

			p.removeMetadata("loggedout", FoxtrotPlugin.getInstance());

		}

		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, WrapperPlayServerOpenSignEntity.TYPE) {

			@Override
			public void onPacketSending(PacketEvent event) {

				WrapperPlayServerOpenSignEntity packet = new WrapperPlayServerOpenSignEntity(event.getPacket());
				Player player = event.getPlayer();
				Location loc = new Location(player.getWorld(), packet.getX(), packet.getY(), packet.getZ());

				if (loc.getBlock().getState().hasMetadata("noSignPacket")) {
					event.setCancelled(true);

				}

			}
		});
		/*
		 * ProtocolLibrary.getProtocolManager().addPacketListener(new
		 * PacketAdapter(this, PacketType.Play.Server.PLAYER_INFO) {
		 * 
		 * @Override public void onPacketSending(PacketEvent event) {
		 * 
		 * String name = event.getPacket().getStrings().read(0);
		 * 
		 * if (!name.endsWith(" ")) { event.setCancelled(true); } else {
		 * event.getPacket().getStrings().write(0, name); }
		 * 
		 * } });
		 */
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_METADATA) {
			public void onPacketSending(PacketEvent event) {
				try {
					Player observer = event.getPlayer();
					StructureModifier<Entity> entityModifer = event.getPacket().getEntityModifier(observer.getWorld());
					org.bukkit.entity.Entity entity = entityModifer.read(0);
					if (entity != null && observer != entity && entity instanceof LivingEntity && !(entity instanceof EnderDragon && entity instanceof Wither) && (entity.getPassenger() == null || entity.getPassenger() != observer)) {
						event.setPacket(event.getPacket().deepClone());
						StructureModifier<List<WrappedWatchableObject>> watcher = event.getPacket().getWatchableCollectionModifier();
						for (WrappedWatchableObject watch : watcher.read(0)) {
							if (watch.getIndex() == 6) {
								if ((Float) watch.getValue() > 0) {
									watch.setValue(RANDOM.nextInt((int) ((Damageable) entity).getMaxHealth()) + new Random().nextFloat());
								}
							}
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		MountainHandler.load();
	}

	@Override
	public void onDisable() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			playtimeMap.playerQuit(p);
			NametagManager.getTeamMap().remove(p.getName());
			NametagManager.cleanupTeams(p);

			p.setMetadata("loggedout", new FixedMetadataValue(this, true));
			p.removeMetadata("subTitle", this);

		}

		for (String str : Kit.getEquippedKits().keySet()) {
			Player p = Bukkit.getPlayerExact(str);

			Kit.getEquippedKits().get(str).remove(p);
		}

        // Remove combat loggers on shutdown.
        for (Villager v : FoxListener.getCombatLoggers().values()) {
            v.remove();
        }

		RedisSaveTask.getInstance().save();
		MountainHandler.reset();
	}

	public <T> T runJedisCommand(JedisCommand<T> jedis){
		Jedis j = pool.getResource();
		T obj = null;

		try{
			obj = jedis.execute(j);
			pool.returnResource(j);
		} catch(JedisException e){
			pool.returnBrokenResource(j);
		} finally {
			pool.returnResource(j);
		}

		return obj;
	}

	private void setupPersistence() {
		playtimeMap = new PlaytimeMap();
		playtimeMap.loadFromRedis();

		oppleMap = new OppleMap();
		oppleMap.loadFromRedis();

		deathbanMap = new DeathbanMap();
		deathbanMap.loadFromRedis();

		joinTimerMap = new JoinTimerMap();
		joinTimerMap.loadFromRedis();

		killsMap = new KillsMap();
		killsMap.loadFromRedis();

        chatModeMap = new ChatModeMap();
        chatModeMap.loadFromRedis();

        toggleLightningMap = new ToggleLightningMap();
        toggleLightningMap.loadFromRedis();

        fishingKitMap = new FishingKitMap();
        fishingKitMap.loadFromRedis();
	}

    public List<String> getConsoleLog(){
        List<String> log = new ArrayList<String>();

        try{
            FileInputStream input = new FileInputStream(new File("logs", "latest.log"));
            BufferedReader br = new BufferedReader(new InputStreamReader(input));
            String strLine;

            while((strLine = br.readLine()) != null){
                log.add(strLine);
            }

            br.close();
            input.close();
        } catch(Exception e){
            log.add("Error reading log file!");
        }

        return log;
    }

	/**
	 * Singleton instance getter
	 * 
	 * @return instance
	 */
	public static FoxtrotPlugin getInstance() {
		return instance;
	}

}
