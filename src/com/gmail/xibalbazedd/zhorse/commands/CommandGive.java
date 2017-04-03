package com.gmail.xibalbazedd.zhorse.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.AbstractHorse;

import com.gmail.xibalbazedd.zhorse.ZHorse;
import com.gmail.xibalbazedd.zhorse.database.HorseRecord;
import com.gmail.xibalbazedd.zhorse.enums.LocaleEnum;

public class CommandGive extends AbstractCommand {

	public CommandGive(ZHorse zh, CommandSender s, String[] a) {
		super(zh, s, a);
		targetIsOwner = false;
		if (isPlayer() && analyseArguments() && hasPermission() && isWorldEnabled() && applyArgument(false)) {
			if (!targetMode) {
				sendCommandUsage();
			}
			else {
				if (isRegistered(targetUUID)) {
					if (!idMode) {
						boolean ownsHorse = ownsHorse(p.getUniqueId(), true);
						if (isOnHorse(ownsHorse)) {
							horse = (AbstractHorse) p.getVehicle();
							if (isRegistered(horse)) {
								execute();
							}
						}
						else if (ownsHorse) {
							horseID = zh.getDM().getPlayerFavoriteHorseID(p.getUniqueId()).toString();
							if (isRegistered(p.getUniqueId(), horseID)) {
								horse = zh.getHM().getFavoriteHorse(p.getUniqueId());
								if (isHorseLoaded(false)) {
									execute();
								}
							}
						}
					}
					else {
						if (isRegistered(p.getUniqueId(), horseID, true)) {
							horse = zh.getHM().getHorse(p.getUniqueId(), Integer.parseInt(horseID));
							if (isHorseLoaded(false)) {
								execute();
							}
						}
					}
				}
			}
		}
	}
	
	private void execute() {
		if (!hasReachedClaimsLimit(true) && isOwner() && isPlayerDifferent() && zh.getEM().canAffordCommand(p, command)) {
			int horseID = zh.getDM().getNextHorseID(targetUUID);
			horseName = zh.getDM().getHorseName(horse.getUniqueId());
			boolean lock = zh.getCM().shouldLockOnClaim();
			boolean protect = zh.getCM().shouldProtectOnClaim();
			boolean share = zh.getCM().shouldShareOnClaim();
			HorseRecord horseRecord = new HorseRecord(horse.getUniqueId().toString(), targetUUID.toString(), horseID, horseName, lock, protect, share, horse.getLocation());
			boolean success = zh.getDM().removeSale(horse.getUniqueId());
			success &= zh.getDM().removeHorse(horse.getUniqueId(), p.getUniqueId());
			success &= zh.getDM().registerHorse(horseRecord);
			if (success) {
				applyHorseName(targetUUID);
				if (displayConsole) {
					zh.getMM().sendMessageHorsePlayer(s, LocaleEnum.HORSE_GIVEN, horseName, targetName);
					if (isPlayerOnline(targetUUID, true)) {
						zh.getMM().sendMessageHorsePlayer(((CommandSender) zh.getServer().getPlayer(targetUUID)), LocaleEnum.HORSE_RECEIVED, horseName, p.getName());
					}
				}
				zh.getEM().payCommand(p, command);
			}
		}
	}

}
