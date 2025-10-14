package com.sparky.libx.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.sparky.libx.region.Region;
import com.sparky.libx.region.RegionManager;
import com.sparky.libx.visualization.RegionVisualizer;

/**
 * Основная команда для управления регионами
 */
public class RegionCommand implements CommandExecutor, TabCompleter {

    private final RegionManager regionManager;
    private final RegionVisualizer visualizer;
    private final Map<UUID, Location[]> selection = new HashMap<>();

    public RegionCommand(RegionManager regionManager, RegionVisualizer visualizer) {
        this.regionManager = regionManager;
        this.visualizer = visualizer;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, 
                           String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда доступна только игрокам!");
            return true;
        }

        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                handleCreate(player, args);
                break;
            case "delete":
                handleDelete(player, args);
                break;
            case "list":
                handleList(player);
                break;
            case "info":
                handleInfo(player, args);
                break;
            case "select":
                handleSelect(player, args);
                break;
            case "visualize":
                handleVisualize(player, args);
                break;
            case "pos1":
            case "pos2":
                handlePositionSelect(player, args[0]);
                break;
            default:
                sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Помощь по командам регионов ===");
        player.sendMessage(ChatColor.YELLOW + "/region pos1 - Установить первую позицию выделения");
        player.sendMessage(ChatColor.YELLOW + "/region pos2 - Установить вторую позицию выделения");
        player.sendMessage(ChatColor.YELLOW + "/region create <тип> <название> [параметры] - Создать регион");
        player.sendMessage(ChatColor.YELLOW + "  Типы: cuboid, sphere, polygon");
        player.sendMessage(ChatColor.YELLOW + "/region delete <название> - Удалить регион");
        player.sendMessage(ChatColor.YELLOW + "/region list - Список регионов");
        player.sendMessage(ChatColor.YELLOW + "/region info <название> - Информация о регионе");
        player.sendMessage(ChatColor.YELLOW + "/region select <название> - Выбрать регион");
        player.sendMessage(ChatColor.YELLOW + "/region visualize <название> [стиль] - Визуализировать регион");
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Использование: /region create <тип> <название> [параметры]");
            return;
        }

        String type = args[1].toLowerCase();
        String name = args[2];
        
        try {
            Region region = createRegion(player, type, name, Arrays.copyOfRange(args, 3, args.length));
            if (region != null) {
                regionManager.registerRegion(region);
                player.sendMessage(ChatColor.GREEN + String.format("Регион '%s' успешно создан!", name));
                visualizer.visualize(player, region, "selection");
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Ошибка: " + e.getMessage());
        }
    }

    private Region createRegion(Player player, String type, String name, String[] params) {
        Location[] points = selection.get(player.getUniqueId());
        if (points == null || points[0] == null || points[1] == null) {
            throw new IllegalArgumentException("Сначала выберите две точки с помощью /region pos1 и /region pos2");
        }

        switch (type) {
            case "cuboid":
                return new com.sparky.libx.region.CuboidRegion(name, points[0], points[1]);
            case "sphere":
                double radius = points[0].distance(points[1]);
                if (params.length > 0) {
                    try {
                        radius = Double.parseDouble(params[0]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Некорректный радиус сферы");
                    }
                }
                return new com.sparky.libx.region.SphereRegion(name, points[0], radius);
            case "polygon":
                throw new UnsupportedOperationException("Создание полигонов через команду пока не поддерживается");
            default:
                throw new IllegalArgumentException("Неизвестный тип региона: " + type);
        }
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /region delete <название>");
            return;
        }

        String name = args[1];
        Region region = regionManager.getRegion(name);
        if (region != null) {
            regionManager.unregisterRegion(name);
            player.sendMessage(ChatColor.GREEN + String.format("Регион '%s' удален", name));
        } else {
            player.sendMessage(ChatColor.RED + String.format("Регион '%s' не найден", name));
        }
    }

    private void handleList(Player player) {
        Collection<Region> regions = regionManager.getRegionsAt(player.getLocation());
        if (regions.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "В этом месте нет регионов");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== Регионы в этой области ===");
        for (Region region : regions) {
            player.sendMessage(String.format("%s- %s (%s)", 
                ChatColor.YELLOW, 
                region.getName(), 
                region.getClass().getSimpleName()));
        }
    }

    private void handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /region info <название>");
            return;
        }

        String name = args[1];
        Region region = regionManager.getRegion(name);
        if (region == null) {
            player.sendMessage(ChatColor.RED + String.format("Регион '%s' не найден", name));
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== Информация о регионе ===");
        player.sendMessage(ChatColor.YELLOW + "Название: " + region.getName());
        player.sendMessage(ChatColor.YELLOW + "Тип: " + region.getClass().getSimpleName());
        player.sendMessage(ChatColor.YELLOW + "Мир: " + region.getWorld().getName());
        player.sendMessage(ChatColor.YELLOW + "Объем: " + String.format("%.1f", region.getVolume()) + " блоков");
        
        Location center = region.getCenter();
        player.sendMessage(ChatColor.YELLOW + String.format("Центр: %.1f, %.1f, %.1f", 
            center.getX(), center.getY(), center.getZ()));
    }

    private void handleSelect(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /region select <название>");
            return;
        }

        String name = args[1];
        Region region = regionManager.getRegion(name);
        if (region == null) {
            player.sendMessage(ChatColor.RED + String.format("Регион '%s' не найден", name));
            return;
        }

        Location[] points = new Location[2];
        points[0] = region.getMinPoint();
        points[1] = region.getMaxPoint();
        selection.put(player.getUniqueId(), points);
        
        player.sendMessage(ChatColor.GREEN + String.format("Регион '%s' выбран. Точки выделения обновлены.", name));
        visualizer.visualize(player, region, "selection");
    }

    private void handleVisualize(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /region visualize <название> [стиль]");
            return;
        }

        String name = args[1];
        String style = args.length > 2 ? args[2] : "default";
        
        Region region = regionManager.getRegion(name);
        if (region == null) {
            player.sendMessage(ChatColor.RED + String.format("Регион '%s' не найден", name));
            return;
        }

        visualizer.visualize(player, region, style);
        player.sendMessage(ChatColor.GREEN + String.format("Визуализация региона '%s' активирована (стиль: %s)", name, style));
    }

    private void handlePositionSelect(Player player, String pos) {
        Location[] points = selection.computeIfAbsent(player.getUniqueId(), k -> new Location[2]);
        
        if (pos.equalsIgnoreCase("pos1")) {
            points[0] = player.getLocation();
            player.sendMessage(ChatColor.GREEN + "Первая позиция установлена: " + formatLocation(points[0]));
        } else {
            points[1] = player.getLocation();
            player.sendMessage(ChatColor.GREEN + "Вторая позиция установлена: " + formatLocation(points[1]));
        }
        
        if (points[0] != null && points[1] != null) {
            Region tempRegion = new com.sparky.libx.region.CuboidRegion(
                "selection", points[0], points[1]);
            visualizer.visualize(player, tempRegion, "selection");
        }
    }

    private String formatLocation(Location loc) {
        return String.format("Мир: %s, X: %.1f, Y: %.1f, Z: %.1f",
            loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, 
                                     String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("create", "delete", "list", "info", "select", "visualize", "pos1", "pos2");
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "create":
                    return Arrays.asList("cuboid", "sphere", "polygon");
                case "delete":
                case "info":
                case "select":
                case "visualize":
                    return regionManager.getRegions().stream()
                        .map(Region::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            if (args[1].equalsIgnoreCase("sphere")) {
                return Collections.singletonList("<радиус>");
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("visualize")) {
            return Arrays.asList("default", "selection", "highlight");
        }

        return Collections.emptyList();
    }
}