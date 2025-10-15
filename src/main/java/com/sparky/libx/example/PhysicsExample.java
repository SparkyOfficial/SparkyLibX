package com.sparky.libx.example;

import org.bukkit.plugin.java.JavaPlugin;

import com.sparky.libx.math.Vector3D;
import com.sparky.libx.physics.Explosion;
import com.sparky.libx.physics.FluidVolume;
import com.sparky.libx.physics.GravityField;
import com.sparky.libx.physics.PhysicsEngine;
import com.sparky.libx.physics.PhysicsEntity;
import com.sparky.libx.physics.RepulsionField;
import com.sparky.libx.physics.VortexField;
import com.sparky.libx.geometry.BoundingBox;

/**
 * приклад використання фізичного рушія
 * демонструє створення складних фізичних взаємодій
 * @author Андрій Будильников
 */
public class PhysicsExample extends JavaPlugin {
    
    private PhysicsEngine physicsEngine;
    
    @Override
    public void onEnable() {
        getLogger().info("PhysicsExample plugin включено!");
        
        // ініціалізувати фізичний рушій
        physicsEngine = PhysicsEngine.getInstance();
        
        // створити демонстраційні фізичні об'єкти
        createDemoEntities();
        
        // створити демонстраційні поля сил
        createDemoForceFields();
        
        // створити демонстраційні об'єми рідин
        createDemoFluidVolumes();
        
        getLogger().info("Створено фізичну демонстрацію з " + physicsEngine.getEntityCount() + 
                        " сутностями, " + physicsEngine.getForceFieldCount() + 
                        " полями сил та " + physicsEngine.getFluidVolumeCount() + " об'ємами рідин");
    }
    
    /**
     * створити демонстраційні фізичні сутності
     */
    private void createDemoEntities() {
        // створити кілька фізичних сутностей
        PhysicsEntity entity1 = new PhysicsEntity(new Vector3D(0, 10, 0), 5.0, 1.0);
        entity1.setName("М'яч 1");
        physicsEngine.addEntity(entity1);
        
        PhysicsEntity entity2 = new PhysicsEntity(new Vector3D(5, 15, 5), 10.0, 2.0);
        entity2.setName("Ящик 1");
        physicsEngine.addEntity(entity2);
        
        PhysicsEntity entity3 = new PhysicsEntity(new Vector3D(-5, 20, -5), 2.0, 0.5);
        entity3.setName("Крапля води");
        physicsEngine.addEntity(entity3);
    }
    
    /**
     * створити демонстраційні поля сил
     */
    private void createDemoForceFields() {
        // створити поле гравітації
        GravityField gravityField = new GravityField(
            new Vector3D(0, 0, 0),    // позиція
            100.0,                    // сила
            50.0,                     // радіус
            new Vector3D(0, -1, 0),   // напрямок (вниз)
            9.81                      // величина
        );
        physicsEngine.addForceField(gravityField);
        
        // створити поле відштовхування
        RepulsionField repulsionField = new RepulsionField(
            new Vector3D(0, 5, 0),    // позиція
            50.0,                     // сила
            20.0,                     // радіус
            0.1                       // швидкість зменшення
        );
        physicsEngine.addForceField(repulsionField);
        
        // створити вихрове поле
        VortexField vortexField = new VortexField(
            new Vector3D(10, 10, 10), // позиція
            75.0,                     // сила
            30.0,                     // радіус
            new Vector3D(0, 1, 0),    // вісь обертання (Y-вісь)
            2.0,                      // кутова швидкість
            0.5                       // сила притягання до центру
        );
        physicsEngine.addForceField(vortexField);
    }
    
    /**
     * створити демонстраційні об'єми рідин
     */
    private void createDemoFluidVolumes() {
        // створити об'єм води
        BoundingBox waterBounds = new BoundingBox(
            new Vector3D(-10, 0, -10),
            new Vector3D(10, 5, 10)
        );
        FluidVolume waterVolume = new FluidVolume(
            waterBounds,
            1000.0,    // густина води (кг/м³)
            0.5        // коефіцієнт опору
        );
        waterVolume.setName("Водяний об'єм");
        physicsEngine.addFluidVolume(waterVolume);
        
        // створити об'єм повітря
        BoundingBox airBounds = new BoundingBox(
            new Vector3D(-50, 0, -50),
            new Vector3D(50, 100, 50)
        );
        FluidVolume airVolume = new FluidVolume(
            airBounds,
            1.225,     // густина повітря (кг/м³)
            0.01       // коефіцієнт опору
        );
        airVolume.setName("Повітряний об'єм");
        physicsEngine.addFluidVolume(airVolume);
    }
    
    /**
     * продемонструвати вибух
     */
    public void demonstrateExplosion() {
        getLogger().info("Створення вибуху...");
        
        Explosion explosion = new Explosion(
            new Vector3D(0, 10, 0),  // позиція
            15.0,                    // радіус
            1000.0                   // сила
        );
        
        explosion.apply(physicsEngine);
        getLogger().info("Вибух створено: " + explosion);
    }
    
    /**
     * оновити фізичний рушій
     */
    public void updatePhysics() {
        physicsEngine.update();
    }
    
    /**
     * отримати фізичний рушій
     */
    public PhysicsEngine getPhysicsEngine() {
        return physicsEngine;
    }
    
    @Override
    public void onDisable() {
        getLogger().info("PhysicsExample plugin вимкнено!");
        
        // очистити фізичний рушій
        if (physicsEngine != null) {
            physicsEngine.clear();
        }
    }
}