package com.sparky.libx.example;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sparky.libx.procedural.StructureGenerator;

/**
 * Пример использования продвинутых возможностей генерации структур
 * @author Андрій Будильников
 */
public class AdvancedStructureExample extends JavaPlugin implements CommandExecutor {
    
    @Override
    public void onEnable() {
        getLogger().info("AdvancedStructureExample plugin включен!");
        getLogger().info("Используйте /generate <structure> для создания структур");
        
        this.getCommand("generate").setExecutor(this);
    }
    
    @Override
    public void onDisable() {
        getLogger().info("AdvancedStructureExample plugin выключен!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эту команду может использовать только игрок!");
            return true;
        }
        
        Player player = (Player) sender;
        World world = player.getWorld();
        Location location = player.getLocation();
        
        if (args.length == 0) {
            player.sendMessage("Использование: /generate <tree|cave|tower|maze>");
            return true;
        }
        
        String structureType = args[0].toLowerCase();
        
        try {
            switch (structureType) {
                case "tree":
                    generateTree(world, location, player);
                    break;
                case "cave":
                    generateCrystalCave(world, location, player);
                    break;
                case "tower":
                    generateSpiralTower(world, location, player);
                    break;
                case "maze":
                    generateMaze(world, location, player);
                    break;
                default:
                    player.sendMessage("Неизвестная структура: " + structureType);
                    player.sendMessage("Доступные структуры: tree, cave, tower, maze");
                    return true;
            }
        } catch (Exception e) {
            player.sendMessage("Ошибка при генерации структуры: " + e.getMessage());
            getLogger().severe("Ошибка при генерации структуры: " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
    
    private void generateTree(World world, Location location, Player player) {
        getLogger().info("Генерация дерева для игрока " + player.getName());
        
        List<org.bukkit.block.Block> blocks = StructureGenerator.generateTree(
            location, 7, Material.OAK_LOG, Material.OAK_LEAVES
        );
        
        player.sendMessage("Создано дерево из " + blocks.size() + " блоков");
        getLogger().info("Создано дерево из " + blocks.size() + " блоков");
    }
    
    private void generateCrystalCave(World world, Location location, Player player) {
        getLogger().info("Генерация кристальной пещеры для игрока " + player.getName());
        
        List<org.bukkit.block.Block> blocks = StructureGenerator.generateCrystalCave(
            location, 8.0, Material.GLASS, Material.STONE
        );
        
        player.sendMessage("Создана кристальная пещера из " + blocks.size() + " блоков");
        getLogger().info("Создана кристальная пещера из " + blocks.size() + " блоков");
    }
    
    private void generateSpiralTower(World world, Location location, Player player) {
        getLogger().info("Генерация спиральной башни для игрока " + player.getName());
        
        List<org.bukkit.block.Block> blocks = StructureGenerator.generateSpiralTower(
            location, 15, 2.5, Material.STONE, 2.0
        );
        
        player.sendMessage("Создана спиральная башня из " + blocks.size() + " блоков");
        getLogger().info("Создана спиральная башня из " + blocks.size() + " блоков");
    }
    
    private void generateMaze(World world, Location location, Player player) {
        getLogger().info("Генерация лабиринта для игрока " + player.getName());
        
        Location corner2 = location.clone().add(30, 0, 30);
        List<org.bukkit.block.Block> blocks = StructureGenerator.generateMaze(
            location, corner2, Material.COBBLESTONE, 2
        );
        
        player.sendMessage("Создан лабиринт из " + blocks.size() + " блоков");
        getLogger().info("Создан лабиринт из " + blocks.size() + " блоков");
    }
}