package com.sparky.libx.vr;

import com.sparky.libx.math.Vector3D;
import com.sparky.libx.math.Quaternion;
import com.sparky.libx.math.Matrix4x4;
import com.sparky.libx.graphics.Renderer3D;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Virtual Reality Framework for Minecraft Plugins
 * Provides capabilities for creating immersive 3D experiences, VR interactions, and spatial computing
 * 
 * @author Андрій Будильников
 */
public class VirtualReality {
    
    /**
     * Represents a VR headset with tracking capabilities
     */
    public static class Headset {
        private final String id;
        private Vector3D position;
        private Quaternion orientation;
        private Vector3D velocity;
        private Vector3D angularVelocity;
        private double ipd; // Interpupillary distance
        private Display display;
        private TrackingSystem trackingSystem;
        
        public static class Display {
            private final int width;
            private final int height;
            private final double refreshRate;
            private final double fov;
            
            public Display(int width, int height, double refreshRate, double fov) {
                this.width = width;
                this.height = height;
                this.refreshRate = refreshRate;
                this.fov = fov;
            }
            
            public int getWidth() {
                return width;
            }
            
            public int getHeight() {
                return height;
            }
            
            public double getRefreshRate() {
                return refreshRate;
            }
            
            public double getFov() {
                return fov;
            }
        }
        
        public Headset(String id, double ipd, Display display) {
            this.id = id;
            this.position = new Vector3D(0, 0, 0);
            this.orientation = Quaternion.identity();
            this.velocity = new Vector3D(0, 0, 0);
            this.angularVelocity = new Vector3D(0, 0, 0);
            this.ipd = ipd;
            this.display = display;
            this.trackingSystem = new TrackingSystem();
        }
        
        public Vector3D getEyePosition(boolean leftEye) {
            // Calculate eye position based on head position, orientation, and IPD
            Vector3D eyeOffset = new Vector3D(ipd / 2 * (leftEye ? -1 : 1), 0, 0);
            Vector3D rotatedOffset = rotateVectorByQuaternion(eyeOffset, orientation);
            return position.add(rotatedOffset);
        }
        
        public Matrix4x4 getViewMatrix(boolean leftEye) {
            Vector3D eyePosition = getEyePosition(leftEye);
            Vector3D forward = rotateVectorByQuaternion(new Vector3D(0, 0, -1), orientation);
            Vector3D up = rotateVectorByQuaternion(new Vector3D(0, 1, 0), orientation);
            
            // Create view matrix
            Vector3D target = eyePosition.add(forward);
            return createLookAtMatrix(eyePosition, target, up);
        }
        
        public Matrix4x4 getProjectionMatrix() {
            double aspectRatio = (double) display.getWidth() / display.getHeight();
            return createPerspectiveMatrix(display.getFov(), aspectRatio, 0.1, 1000.0);
        }
        
        public void updateTracking(Vector3D newPosition, Quaternion newOrientation) {
            // Calculate velocity and angular velocity
            if (position != null) {
                velocity = newPosition.subtract(position);
            }
            if (orientation != null) {
                // Simplified angular velocity calculation
                angularVelocity = new Vector3D(0, 0, 0); // Would need more complex calculation
            }
            
            position = newPosition;
            orientation = newOrientation;
        }
        
        public String getId() {
            return id;
        }
        
        public Vector3D getPosition() {
            return position;
        }
        
        public Quaternion getOrientation() {
            return orientation;
        }
        
        public Vector3D getVelocity() {
            return velocity;
        }
        
        public Vector3D getAngularVelocity() {
            return angularVelocity;
        }
        
        public double getIpd() {
            return ipd;
        }
        
        public Display getDisplay() {
            return display;
        }
        
        public TrackingSystem getTrackingSystem() {
            return trackingSystem;
        }
    }
    
    /**
     * Represents a VR controller with input capabilities
     */
    public static class Controller {
        private final String id;
        private final ControllerHand hand;
        private Vector3D position;
        private Quaternion orientation;
        private Vector3D velocity;
        private Vector3D angularVelocity;
        private final Map<Button, Boolean> buttonStates;
        private final Map<Axis, Vector3D> axisStates;
        private TrackingSystem trackingSystem;
        
        public enum ControllerHand {
            LEFT, RIGHT
        }
        
        public enum Button {
            TRIGGER, GRIP, THUMBSTICK, A, B, X, Y
        }
        
        public enum Axis {
            THUMBSTICK, TOUCHPAD
        }
        
        public Controller(String id, ControllerHand hand) {
            this.id = id;
            this.hand = hand;
            this.position = new Vector3D(0, 0, 0);
            this.orientation = Quaternion.identity();
            this.velocity = new Vector3D(0, 0, 0);
            this.angularVelocity = new Vector3D(0, 0, 0);
            this.buttonStates = new ConcurrentHashMap<>();
            this.axisStates = new ConcurrentHashMap<>();
            this.trackingSystem = new TrackingSystem();
            
            // Initialize button states
            for (Button button : Button.values()) {
                buttonStates.put(button, false);
            }
            
            // Initialize axis states
            for (Axis axis : Axis.values()) {
                axisStates.put(axis, new Vector3D(0, 0, 0));
            }
        }
        
        public void updateTracking(Vector3D newPosition, Quaternion newOrientation) {
            // Calculate velocity and angular velocity
            if (position != null) {
                velocity = newPosition.subtract(position);
            }
            if (orientation != null) {
                // Simplified angular velocity calculation
                angularVelocity = new Vector3D(0, 0, 0); // Would need more complex calculation
            }
            
            position = newPosition;
            orientation = newOrientation;
        }
        
        public void setButtonState(Button button, boolean pressed) {
            buttonStates.put(button, pressed);
        }
        
        public void setAxisState(Axis axis, Vector3D value) {
            axisStates.put(axis, value);
        }
        
        public Vector3D getPointerDirection() {
            // Pointing direction is typically along the negative Z axis of the controller
            return rotateVectorByQuaternion(new Vector3D(0, 0, -1), orientation);
        }
        
        public Ray getPointingRay() {
            return new Ray(position, getPointerDirection());
        }
        
        public String getId() {
            return id;
        }
        
        public ControllerHand getHand() {
            return hand;
        }
        
        public Vector3D getPosition() {
            return position;
        }
        
        public Quaternion getOrientation() {
            return orientation;
        }
        
        public Vector3D getVelocity() {
            return velocity;
        }
        
        public Vector3D getAngularVelocity() {
            return angularVelocity;
        }
        
        public boolean isButtonPressed(Button button) {
            return buttonStates.getOrDefault(button, false);
        }
        
        public Vector3D getAxisValue(Axis axis) {
            return axisStates.getOrDefault(axis, new Vector3D(0, 0, 0));
        }
        
        public TrackingSystem getTrackingSystem() {
            return trackingSystem;
        }
    }
    
    /**
     * Represents a 3D ray for intersection testing
     */
    public static class Ray {
        private final Vector3D origin;
        private final Vector3D direction;
        
        public Ray(Vector3D origin, Vector3D direction) {
            this.origin = origin;
            this.direction = direction.normalize();
        }
        
        public Vector3D getOrigin() {
            return origin;
        }
        
        public Vector3D getDirection() {
            return direction;
        }
        
        public Vector3D at(double t) {
            return origin.add(direction.multiply(t));
        }
    }
    
    /**
     * Represents a tracking system for VR devices
     */
    public static class TrackingSystem {
        private TrackingType type;
        private TrackingQuality quality;
        private boolean isTracking;
        private long lastUpdate;
        
        public enum TrackingType {
            OPTICAL, LASER, INERTIAL, HYBRID
        }
        
        public enum TrackingQuality {
            LOW, MEDIUM, HIGH, ULTRA
        }
        
        public TrackingSystem() {
            this.type = TrackingType.HYBRID;
            this.quality = TrackingQuality.HIGH;
            this.isTracking = false;
            this.lastUpdate = 0;
        }
        
        public void updateTracking(Vector3D position, Quaternion orientation) {
            // In a real implementation, this would update the tracking system
            this.isTracking = true;
            this.lastUpdate = System.currentTimeMillis();
        }
        
        public boolean isTrackingValid() {
            // Check if tracking is recent enough
            return isTracking && (System.currentTimeMillis() - lastUpdate) < 100; // 100ms timeout
        }
        
        public TrackingType getType() {
            return type;
        }
        
        public void setType(TrackingType type) {
            this.type = type;
        }
        
        public TrackingQuality getQuality() {
            return quality;
        }
        
        public void setQuality(TrackingQuality quality) {
            this.quality = quality;
        }
        
        public boolean isTracking() {
            return isTracking;
        }
        
        public long getLastUpdate() {
            return lastUpdate;
        }
    }
    
    /**
     * Represents a VR scene with objects and interactions
     */
    public static class VRScene {
        private final String name;
        private final List<VRObject> objects;
        private final List<VRInteraction> interactions;
        private Vector3D gravity;
        private Vector3D ambientLight;
        private List<Renderer3D.Light> lights;
        
        public VRScene(String name) {
            this.name = name;
            this.objects = new CopyOnWriteArrayList<>();
            this.interactions = new CopyOnWriteArrayList<>();
            this.gravity = new Vector3D(0, -9.81, 0);
            this.ambientLight = new Vector3D(0.2, 0.2, 0.2);
            this.lights = new CopyOnWriteArrayList<>();
        }
        
        public void addObject(VRObject object) {
            objects.add(object);
        }
        
        public void removeObject(VRObject object) {
            objects.remove(object);
        }
        
        public void addInteraction(VRInteraction interaction) {
            interactions.add(interaction);
        }
        
        public void removeInteraction(VRInteraction interaction) {
            interactions.remove(interaction);
        }
        
        public List<VRObject> getObjects() {
            return new ArrayList<>(objects);
        }
        
        public List<VRInteraction> getInteractions() {
            return new ArrayList<>(interactions);
        }
        
        public void update(double deltaTime) {
            // Update all objects
            for (VRObject object : objects) {
                object.update(deltaTime);
            }
            
            // Update all interactions
            for (VRInteraction interaction : interactions) {
                interaction.update(deltaTime);
            }
        }
        
        public IntersectionResult intersectRay(Ray ray) {
            IntersectionResult closestIntersection = null;
            double closestDistance = Double.MAX_VALUE;
            
            for (VRObject object : objects) {
                IntersectionResult result = object.intersectRay(ray);
                if (result != null && result.getDistance() < closestDistance) {
                    closestIntersection = result;
                    closestDistance = result.getDistance();
                }
            }
            
            return closestIntersection;
        }
        
        public String getName() {
            return name;
        }
        
        public Vector3D getGravity() {
            return gravity;
        }
        
        public void setGravity(Vector3D gravity) {
            this.gravity = gravity;
        }
        
        public Vector3D getAmbientLight() {
            return ambientLight;
        }
        
        public void setAmbientLight(Vector3D ambientLight) {
            this.ambientLight = ambientLight;
        }
        
        public List<Renderer3D.Light> getLights() {
            return new ArrayList<>(lights);
        }
        
        public void addLight(Renderer3D.Light light) {
            lights.add(light);
        }
        
        public void removeLight(Renderer3D.Light light) {
            lights.remove(light);
        }
    }
    
    /**
     * Represents a virtual object in the VR scene
     */
    public static class VRObject {
        private final String id;
        private Vector3D position;
        private Quaternion orientation;
        private Vector3D scale;
        private Renderer3D.Mesh mesh;
        private boolean visible;
        private boolean collidable;
        private PhysicsProperties physicsProperties;
        private List<VRComponent> components;
        
        public static class PhysicsProperties {
            private double mass;
            private double friction;
            private double restitution;
            private boolean isStatic;
            
            public PhysicsProperties(double mass, double friction, double restitution, boolean isStatic) {
                this.mass = mass;
                this.friction = friction;
                this.restitution = restitution;
                this.isStatic = isStatic;
            }
            
            public double getMass() {
                return mass;
            }
            
            public double getFriction() {
                return friction;
            }
            
            public double getRestitution() {
                return restitution;
            }
            
            public boolean isStatic() {
                return isStatic;
            }
        }
        
        public VRObject(String id, Renderer3D.Mesh mesh) {
            this.id = id;
            this.position = new Vector3D(0, 0, 0);
            this.orientation = Quaternion.identity();
            this.scale = new Vector3D(1, 1, 1);
            this.mesh = mesh;
            this.visible = true;
            this.collidable = true;
            this.physicsProperties = new PhysicsProperties(1.0, 0.5, 0.3, false);
            this.components = new CopyOnWriteArrayList<>();
        }
        
        public Matrix4x4 getTransformMatrix() {
            Matrix4x4 translationMatrix = Matrix4x4.createTranslation(position.getX(), position.getY(), position.getZ());
            Matrix4x4 rotationMatrix = quaternionToMatrix(orientation);
            Matrix4x4 scaleMatrix = Matrix4x4.createScale(scale.getX(), scale.getY(), scale.getZ());
            
            return translationMatrix.multiply(rotationMatrix).multiply(scaleMatrix);
        }
        
        public void update(double deltaTime) {
            // Update all components
            for (VRComponent component : components) {
                component.update(deltaTime);
            }
        }
        
        public IntersectionResult intersectRay(Ray ray) {
            // Simplified ray-object intersection
            // In a real implementation, this would perform proper mesh intersection testing
            
            // Transform ray to object space
            Matrix4x4 invTransform = getInverseMatrix(getTransformMatrix());
            Vector3D localOrigin = transformPoint(invTransform, ray.getOrigin());
            Vector3D localDirection = transformVector(invTransform, ray.getDirection());
            Ray localRay = new Ray(localOrigin, localDirection);
            
            // Simple bounding sphere test
            if (mesh != null) {
                // Calculate bounding sphere
                Vector3D center = new Vector3D(0, 0, 0);
                double radius = 1.0; // Simplified
                
                // Ray-sphere intersection
                Vector3D oc = localRay.getOrigin().subtract(center);
                double a = localRay.getDirection().dot(localRay.getDirection());
                double b = 2.0 * oc.dot(localRay.getDirection());
                double c = oc.dot(oc) - radius * radius;
                double discriminant = b * b - 4 * a * c;
                
                if (discriminant >= 0) {
                    double t = (-b - Math.sqrt(discriminant)) / (2.0 * a);
                    if (t >= 0) {
                        Vector3D intersectionPoint = ray.at(t);
                        Vector3D normal = intersectionPoint.subtract(position).normalize();
                        return new IntersectionResult(this, intersectionPoint, normal, t);
                    }
                }
            }
            
            return null;
        }
        
        public String getId() {
            return id;
        }
        
        public Vector3D getPosition() {
            return position;
        }
        
        public void setPosition(Vector3D position) {
            this.position = position;
        }
        
        public Quaternion getOrientation() {
            return orientation;
        }
        
        public void setOrientation(Quaternion orientation) {
            this.orientation = orientation;
        }
        
        public Vector3D getScale() {
            return scale;
        }
        
        public void setScale(Vector3D scale) {
            this.scale = scale;
        }
        
        public Renderer3D.Mesh getMesh() {
            return mesh;
        }
        
        public void setMesh(Renderer3D.Mesh mesh) {
            this.mesh = mesh;
        }
        
        public boolean isVisible() {
            return visible;
        }
        
        public void setVisible(boolean visible) {
            this.visible = visible;
        }
        
        public boolean isCollidable() {
            return collidable;
        }
        
        public void setCollidable(boolean collidable) {
            this.collidable = collidable;
        }
        
        public PhysicsProperties getPhysicsProperties() {
            return physicsProperties;
        }
        
        public void setPhysicsProperties(PhysicsProperties physicsProperties) {
            this.physicsProperties = physicsProperties;
        }
        
        public List<VRComponent> getComponents() {
            return new ArrayList<>(components);
        }
        
        public void addComponent(VRComponent component) {
            components.add(component);
        }
        
        public void removeComponent(VRComponent component) {
            components.remove(component);
        }
    }
    
    /**
     * Represents a component that can be attached to VR objects
     */
    public abstract static class VRComponent {
        protected final String name;
        protected VRObject parent;
        protected boolean enabled;
        
        public VRComponent(String name) {
            this.name = name;
            this.enabled = true;
        }
        
        public abstract void update(double deltaTime);
        
        public String getName() {
            return name;
        }
        
        public VRObject getParent() {
            return parent;
        }
        
        public void setParent(VRObject parent) {
            this.parent = parent;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    /**
     * Represents an interaction between VR controllers and objects
     */
    public static class VRInteraction {
        private final String id;
        private final Controller controller;
        private final VRObject targetObject;
        private InteractionType type;
        private boolean isActive;
        private double startTime;
        private List<InteractionCallback> callbacks;
        
        public enum InteractionType {
            GRAB, TOUCH, POINT, HAPTIC
        }
        
        public interface InteractionCallback {
            void onInteractionStart(VRInteraction interaction);
            void onInteractionUpdate(VRInteraction interaction, double deltaTime);
            void onInteractionEnd(VRInteraction interaction);
        }
        
        public VRInteraction(String id, Controller controller, VRObject targetObject, InteractionType type) {
            this.id = id;
            this.controller = controller;
            this.targetObject = targetObject;
            this.type = type;
            this.isActive = false;
            this.startTime = 0;
            this.callbacks = new CopyOnWriteArrayList<>();
        }
        
        public void update(double deltaTime) {
            if (!isActive) {
                // Check if interaction should start
                if (shouldStartInteraction()) {
                    startInteraction();
                }
            } else {
                // Update active interaction
                for (InteractionCallback callback : callbacks) {
                    callback.onInteractionUpdate(this, deltaTime);
                }
                
                // Check if interaction should end
                if (shouldEndInteraction()) {
                    endInteraction();
                }
            }
        }
        
        private boolean shouldStartInteraction() {
            switch (type) {
                case GRAB:
                    return controller.isButtonPressed(Controller.Button.TRIGGER) && 
                           isControllerPointingAtObject();
                case TOUCH:
                    return isControllerCloseToObject();
                case POINT:
                    return isControllerPointingAtObject();
                default:
                    return false;
            }
        }
        
        private boolean shouldEndInteraction() {
            switch (type) {
                case GRAB:
                    return !controller.isButtonPressed(Controller.Button.TRIGGER);
                case TOUCH:
                    return !isControllerCloseToObject();
                case POINT:
                    return !isControllerPointingAtObject();
                default:
                    return true;
            }
        }
        
        private void startInteraction() {
            isActive = true;
            startTime = System.currentTimeMillis();
            for (InteractionCallback callback : callbacks) {
                callback.onInteractionStart(this);
            }
        }
        
        private void endInteraction() {
            isActive = false;
            for (InteractionCallback callback : callbacks) {
                callback.onInteractionEnd(this);
            }
        }
        
        private boolean isControllerPointingAtObject() {
            Ray ray = controller.getPointingRay();
            return targetObject.intersectRay(ray) != null;
        }
        
        private boolean isControllerCloseToObject() {
            double distance = controller.getPosition().distance(targetObject.getPosition());
            return distance < 0.1; // 10cm threshold
        }
        
        public void addCallback(InteractionCallback callback) {
            callbacks.add(callback);
        }
        
        public void removeCallback(InteractionCallback callback) {
            callbacks.remove(callback);
        }
        
        public String getId() {
            return id;
        }
        
        public Controller getController() {
            return controller;
        }
        
        public VRObject getTargetObject() {
            return targetObject;
        }
        
        public InteractionType getType() {
            return type;
        }
        
        public boolean isActive() {
            return isActive;
        }
        
        public double getStartTime() {
            return startTime;
        }
        
        public List<InteractionCallback> getCallbacks() {
            return new ArrayList<>(callbacks);
        }
    }
    
    /**
     * Represents the result of a ray-object intersection
     */
    public static class IntersectionResult {
        private final VRObject object;
        private final Vector3D point;
        private final Vector3D normal;
        private final double distance;
        
        public IntersectionResult(VRObject object, Vector3D point, Vector3D normal, double distance) {
            this.object = object;
            this.point = point;
            this.normal = normal;
            this.distance = distance;
        }
        
        public VRObject getObject() {
            return object;
        }
        
        public Vector3D getPoint() {
            return point;
        }
        
        public Vector3D getNormal() {
            return normal;
        }
        
        public double getDistance() {
            return distance;
        }
    }
    
    /**
     * Represents a VR haptic feedback system
     */
    public static class HapticSystem {
        private final Map<String, HapticDevice> devices;
        
        public static class HapticDevice {
            private final String id;
            private final HapticType type;
            private boolean isEnabled;
            private double intensity;
            
            public enum HapticType {
                VIBRATION, FORCE_FEEDBACK, TEXTURE_FEEDBACK
            }
            
            public HapticDevice(String id, HapticType type) {
                this.id = id;
                this.type = type;
                this.isEnabled = true;
                this.intensity = 0.0;
            }
            
            public void trigger(double intensity, double duration) {
                if (!isEnabled) return;
                
                this.intensity = Math.max(0.0, Math.min(1.0, intensity));
                // In a real implementation, this would send haptic commands to the device
            }
            
            public String getId() {
                return id;
            }
            
            public HapticType getType() {
                return type;
            }
            
            public boolean isEnabled() {
                return isEnabled;
            }
            
            public void setEnabled(boolean enabled) {
                this.isEnabled = enabled;
            }
            
            public double getIntensity() {
                return intensity;
            }
        }
        
        public HapticSystem() {
            this.devices = new ConcurrentHashMap<>();
        }
        
        public void addDevice(HapticDevice device) {
            devices.put(device.getId(), device);
        }
        
        public void removeDevice(String id) {
            devices.remove(id);
        }
        
        public HapticDevice getDevice(String id) {
            return devices.get(id);
        }
        
        public void triggerHaptic(String deviceId, double intensity, double duration) {
            HapticDevice device = devices.get(deviceId);
            if (device != null) {
                device.trigger(intensity, duration);
            }
        }
        
        public Map<String, HapticDevice> getDevices() {
            return new HashMap<>(devices);
        }
    }
    
    /**
     * Represents a VR application system
     */
    public static class VRApplication {
        private final String name;
        private final String version;
        private VRScene currentScene;
        private Headset headset;
        private List<Controller> controllers;
        private HapticSystem hapticSystem;
        private boolean isRunning;
        private long frameCount;
        private double lastFrameTime;
        
        public VRApplication(String name, String version) {
            this.name = name;
            this.version = version;
            this.controllers = new CopyOnWriteArrayList<>();
            this.hapticSystem = new HapticSystem();
            this.isRunning = false;
            this.frameCount = 0;
            this.lastFrameTime = 0;
        }
        
        public void start() {
            if (!isRunning) {
                isRunning = true;
                lastFrameTime = System.nanoTime() / 1_000_000_000.0;
            }
        }
        
        public void stop() {
            isRunning = false;
        }
        
        public void update() {
            if (!isRunning) return;
            
            // Calculate delta time
            double currentTime = System.nanoTime() / 1_000_000_000.0;
            double deltaTime = currentTime - lastFrameTime;
            lastFrameTime = currentTime;
            
            frameCount++;
            
            // Update scene
            if (currentScene != null) {
                currentScene.update(deltaTime);
            }
            
            // Update controllers
            for (Controller controller : controllers) {
                // In a real implementation, this would get updated tracking data
            }
        }
        
        public void render() {
            if (!isRunning) return;
            
            // In a real implementation, this would render the scene for each eye
            // and submit frames to the VR compositor
        }
        
        public void setCurrentScene(VRScene scene) {
            this.currentScene = scene;
        }
        
        public void setHeadset(Headset headset) {
            this.headset = headset;
        }
        
        public void addController(Controller controller) {
            controllers.add(controller);
        }
        
        public void removeController(Controller controller) {
            controllers.remove(controller);
        }
        
        public String getName() {
            return name;
        }
        
        public String getVersion() {
            return version;
        }
        
        public VRScene getCurrentScene() {
            return currentScene;
        }
        
        public Headset getHeadset() {
            return headset;
        }
        
        public List<Controller> getControllers() {
            return new ArrayList<>(controllers);
        }
        
        public HapticSystem getHapticSystem() {
            return hapticSystem;
        }
        
        public boolean isRunning() {
            return isRunning;
        }
        
        public long getFrameCount() {
            return frameCount;
        }
        
        public double getFramerate() {
            return frameCount > 0 ? frameCount / lastFrameTime : 0;
        }
    }
    
    /**
     * Utility method to rotate a vector by a quaternion
     */
    private static Vector3D rotateVectorByQuaternion(Vector3D vector, Quaternion quaternion) {
        // Convert vector to pure quaternion (0, x, y, z)
        Quaternion vectorQuat = new Quaternion(0, vector.getX(), vector.getY(), vector.getZ());
        
        // Apply rotation: q * v * q^-1
        Quaternion rotated = quaternion.multiply(vectorQuat).multiply(quaternion.inverse());
        
        // Extract vector part
        return new Vector3D(rotated.getX(), rotated.getY(), rotated.getZ());
    }
    
    /**
     * Utility method to convert quaternion to rotation matrix
     */
    private static Matrix4x4 quaternionToMatrix(Quaternion q) {
        double w = q.getW();
        double x = q.getX();
        double y = q.getY();
        double z = q.getZ();
        
        Matrix4x4 matrix = new Matrix4x4();
        matrix.set(0, 0, 1 - 2 * (y * y + z * z));
        matrix.set(0, 1, 2 * (x * y - w * z));
        matrix.set(0, 2, 2 * (x * z + w * y));
        matrix.set(1, 0, 2 * (x * y + w * z));
        matrix.set(1, 1, 1 - 2 * (x * x + z * z));
        matrix.set(1, 2, 2 * (y * z - w * x));
        matrix.set(2, 0, 2 * (x * z - w * y));
        matrix.set(2, 1, 2 * (y * z + w * x));
        matrix.set(2, 2, 1 - 2 * (x * x + y * y));
        matrix.set(3, 3, 1);
        
        return matrix;
    }
    
    /**
     * Utility method to transform a point by a matrix
     */
    private static Vector3D transformPoint(Matrix4x4 matrix, Vector3D point) {
        double x = matrix.get(0, 0) * point.getX() + matrix.get(0, 1) * point.getY() + matrix.get(0, 2) * point.getZ() + matrix.get(0, 3);
        double y = matrix.get(1, 0) * point.getX() + matrix.get(1, 1) * point.getY() + matrix.get(1, 2) * point.getZ() + matrix.get(1, 3);
        double z = matrix.get(2, 0) * point.getX() + matrix.get(2, 1) * point.getY() + matrix.get(2, 2) * point.getZ() + matrix.get(2, 3);
        double w = matrix.get(3, 0) * point.getX() + matrix.get(3, 1) * point.getY() + matrix.get(3, 2) * point.getZ() + matrix.get(3, 3);
        
        // Perspective divide
        if (w != 0) {
            return new Vector3D(x / w, y / w, z / w);
        }
        return new Vector3D(x, y, z);
    }
    
    /**
     * Utility method to transform a vector by a matrix (no translation)
     */
    private static Vector3D transformVector(Matrix4x4 matrix, Vector3D vector) {
        double x = matrix.get(0, 0) * vector.getX() + matrix.get(0, 1) * vector.getY() + matrix.get(0, 2) * vector.getZ();
        double y = matrix.get(1, 0) * vector.getX() + matrix.get(1, 1) * vector.getY() + matrix.get(1, 2) * vector.getZ();
        double z = matrix.get(2, 0) * vector.getX() + matrix.get(2, 1) * vector.getY() + matrix.get(2, 2) * vector.getZ();
        return new Vector3D(x, y, z);
    }
    
    /**
     * Create a look-at matrix for camera/view transformations
     */
    private static Matrix4x4 createLookAtMatrix(Vector3D eye, Vector3D target, Vector3D up) {
        Vector3D forward = target.subtract(eye).normalize();
        Vector3D right = forward.cross(up).normalize();
        Vector3D newUp = right.cross(forward);
        
        Matrix4x4 matrix = new Matrix4x4();
        matrix.set(0, 0, right.getX());
        matrix.set(0, 1, right.getY());
        matrix.set(0, 2, right.getZ());
        matrix.set(1, 0, newUp.getX());
        matrix.set(1, 1, newUp.getY());
        matrix.set(1, 2, newUp.getZ());
        matrix.set(2, 0, -forward.getX());
        matrix.set(2, 1, -forward.getY());
        matrix.set(2, 2, -forward.getZ());
        matrix.set(3, 0, -right.dot(eye));
        matrix.set(3, 1, -newUp.dot(eye));
        matrix.set(3, 2, forward.dot(eye));
        matrix.set(3, 3, 1);
        
        return matrix;
    }
    
    /**
     * Create a perspective projection matrix
     */
    private static Matrix4x4 createPerspectiveMatrix(double fov, double aspectRatio, double near, double far) {
        double f = 1.0 / Math.tan(fov / 2.0);
        double rangeInv = 1.0 / (near - far);
        
        Matrix4x4 matrix = new Matrix4x4();
        matrix.set(0, 0, f / aspectRatio);
        matrix.set(1, 1, f);
        matrix.set(2, 2, (near + far) * rangeInv);
        matrix.set(2, 3, -1);
        matrix.set(3, 2, 2.0 * near * far * rangeInv);
        matrix.set(3, 3, 0);
        
        return matrix;
    }
    
    /**
     * Calculate the inverse of a transformation matrix
     */
    private static Matrix4x4 getInverseMatrix(Matrix4x4 matrix) {
        // Simplified inverse calculation for transformation matrices
        // In a real implementation, this would be more robust
        Matrix4x4 result = new Matrix4x4();
        
        // Transpose the rotation part
        result.set(0, 0, matrix.get(0, 0));
        result.set(0, 1, matrix.get(1, 0));
        result.set(0, 2, matrix.get(2, 0));
        result.set(1, 0, matrix.get(0, 1));
        result.set(1, 1, matrix.get(1, 1));
        result.set(1, 2, matrix.get(2, 1));
        result.set(2, 0, matrix.get(0, 2));
        result.set(2, 1, matrix.get(1, 2));
        result.set(2, 2, matrix.get(2, 2));
        
        // Negate and transform the translation part
        Vector3D translation = new Vector3D(matrix.get(0, 3), matrix.get(1, 3), matrix.get(2, 3));
        Vector3D negatedTranslation = transformVector(result, new Vector3D(-translation.getX(), -translation.getY(), -translation.getZ()));
        result.set(0, 3, negatedTranslation.getX());
        result.set(1, 3, negatedTranslation.getY());
        result.set(2, 3, negatedTranslation.getZ());
        result.set(3, 3, 1);
        
        return result;
    }
}