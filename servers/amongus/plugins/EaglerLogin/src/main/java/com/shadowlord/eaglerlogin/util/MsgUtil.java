package com.shadowlord.eaglerlogin.util;

import org.bukkit.ChatColor;

public final class MsgUtil {
  private MsgUtil() {}
  public static String color(String s) {
    return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
  }
}
