package com.sparky.libx.graphics;

import com.sparky.libx.math.Vector3D;
import com.sparky.libx.math.Matrix4x4;
import java.awt.Color;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 3D graphics renderer with advanced rendering capabilities
 * @author Андрій Будильников
 */
public class Renderer3D {
    
    /**
     * Represents a vertex in 3D space
     */
    public static class Vertex {
        public Vector3D position;
        public Vector3D normal;
        public Vector3D color;
        
        public Vertex(Vector3D position, Vector3D normal, Vector3D color) {
            this.position = position;
            this.normal = normal;
            this.color = color;
        }
        
        public Vertex copy() {
            return new Vertex(new Vector3D(position.getX(), position.getY(), position.getZ()), 
                             new Vector3D(normal.getX(), normal.getY(), normal.getZ()), 
                             new Vector3D(color.getX(), color.getY(), color.getZ()));
        }
    }
    
    /**
     * Represents a triangle formed by three vertices
     */
    public static class Triangle {
        public final Vertex v1, v2, v3;
        
        public Triangle(Vertex v1, Vertex v2, Vertex v3) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
        }
        
        /**
         * Gets the normal vector of the triangle
         */
        public Vector3D getNormal() {
            Vector3D edge1 = v2.position.subtract(v1.position);
            Vector3D edge2 = v3.position.subtract(v1.position);
            return edge1.cross(edge2).normalize();
        }
        
        /**
         * Gets the centroid of the triangle
         */
        public Vector3D getCentroid() {
            return v1.position.add(v2.position).add(v3.position).divide(3);
        }
    }
    
    /**
     * Represents a 3D mesh composed of vertices and triangles
     */
    public static class Mesh {
        public final List<Vertex> vertices;
        public final List<Triangle> triangles;
        
        public Mesh() {
            this.vertices = new CopyOnWriteArrayList<>();
            this.triangles = new CopyOnWriteArrayList<>();
        }
        
        public void addVertex(Vertex vertex) {
            vertices.add(vertex);
        }
        
        public void addTriangle(Triangle triangle) {
            triangles.add(triangle);
        }
        
        public void addTriangle(int v1Index, int v2Index, int v3Index) {
            if (v1Index < vertices.size() && v2Index < vertices.size() && v3Index < vertices.size()) {
                Vertex v1 = vertices.get(v1Index);
                Vertex v2 = vertices.get(v2Index);
                Vertex v3 = vertices.get(v3Index);
                triangles.add(new Triangle(v1, v2, v3));
            }
        }
        
        /**
         * Transforms the mesh using a transformation matrix
         */
        public void transform(Matrix4x4 transformationMatrix) {
            for (Vertex vertex : vertices) {
                vertex.position = transformationMatrix.multiply(vertex.position);
                // Transform normal (requires inverse transpose of transformation matrix)
                // For simplicity, we'll just normalize it
                vertex.normal = vertex.normal.normalize();
            }
        }
        
        /**
         * Calculates normals for all vertices
         */
        public void calculateNormals() {
            // Reset all normals
            for (Vertex vertex : vertices) {
                vertex.normal = new Vector3D(0, 0, 0);
            }
            
            // Accumulate face normals
            for (Triangle triangle : triangles) {
                Vector3D faceNormal = triangle.getNormal();
                triangle.v1.normal = triangle.v1.normal.add(faceNormal);
                triangle.v2.normal = triangle.v2.normal.add(faceNormal);
                triangle.v3.normal = triangle.v3.normal.add(faceNormal);
            }
            
            // Normalize all vertex normals
            for (Vertex vertex : vertices) {
                vertex.normal = vertex.normal.normalize();
            }
        }
        
        /**
         * Gets the bounding box of the mesh
         */
        public BoundingBox getBoundingBox() {
            if (vertices.isEmpty()) {
                return new BoundingBox(new Vector3D(0, 0, 0), new Vector3D(0, 0, 0));
            }
            
            Vector3D min = new Vector3D(vertices.get(0).position.getX(), vertices.get(0).position.getY(), vertices.get(0).position.getZ());
            Vector3D max = new Vector3D(vertices.get(0).position.getX(), vertices.get(0).position.getY(), vertices.get(0).position.getZ());
            
            for (Vertex vertex : vertices) {
                Vector3D pos = vertex.position;
                min = new Vector3D(
                    Math.min(min.getX(), pos.getX()),
                    Math.min(min.getY(), pos.getY()),
                    Math.min(min.getZ(), pos.getZ())
                );
                max = new Vector3D(
                    Math.max(max.getX(), pos.getX()),
                    Math.max(max.getY(), pos.getY()),
                    Math.max(max.getZ(), pos.getZ())
                );
            }
            
            return new BoundingBox(min, max);
        }
    }
    
    /**
     * Represents a bounding box
     */
    public static class BoundingBox {
        public final Vector3D min;
        public final Vector3D max;
        
        public BoundingBox(Vector3D min, Vector3D max) {
            this.min = min;
            this.max = max;
        }
        
        /**
         * Checks if this bounding box intersects with another
         */
        public boolean intersects(BoundingBox other) {
            return !(min.getX() > other.max.getX() || max.getX() < other.min.getX() ||
                     min.getY() > other.max.getY() || max.getY() < other.min.getY() ||
                     min.getZ() > other.max.getZ() || max.getZ() < other.min.getZ());
        }
        
        /**
         * Gets the center of the bounding box
         */
        public Vector3D getCenter() {
            return min.add(max).divide(2);
        }
        
        /**
         * Gets the size of the bounding box
         */
        public Vector3D getSize() {
            return max.subtract(min);
        }
    }
    
    /**
     * Represents a camera for 3D rendering
     */
    public static class Camera {
        private Vector3D position;
        private Vector3D target;
        private Vector3D up;
        private double fov;
        private double aspectRatio;
        private double nearPlane;
        private double farPlane;
        
        public Camera() {
            this.position = new Vector3D(0, 0, 5);
            this.target = new Vector3D(0, 0, 0);
            this.up = new Vector3D(0, 1, 0);
            this.fov = Math.PI / 4; // 45 degrees
            this.aspectRatio = 16.0 / 9.0;
            this.nearPlane = 0.1;
            this.farPlane = 1000.0;
        }
        
        /**
         * Gets the view matrix
         */
        public Matrix4x4 getViewMatrix() {
            Vector3D forward = target.subtract(position).normalize();
            Vector3D right = forward.cross(up).normalize();
            Vector3D newUp = right.cross(forward);
            
            Matrix4x4 viewMatrix = new Matrix4x4(
                new double[][] {
                    {right.getX(), newUp.getX(), -forward.getX(), 0},
                    {right.getY(), newUp.getY(), -forward.getY(), 0},
                    {right.getZ(), newUp.getZ(), -forward.getZ(), 0},
                    {-right.dot(position), -newUp.dot(position), forward.dot(position), 1}
                }
            );
            
            return viewMatrix;
        }
        
        /**
         * Gets the projection matrix
         */
        public Matrix4x4 getProjectionMatrix() {
            double f = 1.0 / Math.tan(fov / 2.0);
            double rangeInv = 1.0 / (nearPlane - farPlane);
            
            Matrix4x4 projectionMatrix = new Matrix4x4(
                new double[][] {
                    {f / aspectRatio, 0, 0, 0},
                    {0, f, 0, 0},
                    {0, 0, (nearPlane + farPlane) * rangeInv, -1},
                    {0, 0, 2 * nearPlane * farPlane * rangeInv, 0}
                }
            );
            
            return projectionMatrix;
        }
        
        // Getters and setters
        public Vector3D getPosition() { return position; }
        public void setPosition(Vector3D position) { this.position = position; }
        public Vector3D getTarget() { return target; }
        public void setTarget(Vector3D target) { this.target = target; }
        public Vector3D getUp() { return up; }
        public void setUp(Vector3D up) { this.up = up; }
        public double getFov() { return fov; }
        public void setFov(double fov) { this.fov = fov; }
        public double getAspectRatio() { return aspectRatio; }
        public void setAspectRatio(double aspectRatio) { this.aspectRatio = aspectRatio; }
        public double getNearPlane() { return nearPlane; }
        public void setNearPlane(double nearPlane) { this.nearPlane = nearPlane; }
        public double getFarPlane() { return farPlane; }
        public void setFarPlane(double farPlane) { this.farPlane = farPlane; }
    }
    
    /**
     * Represents a render context
     */
    public static class RenderContext {
        private final int screenWidth;
        private final int screenHeight;
        private final Camera camera;
        private final List<Light> lights;
        private final Color backgroundColor;
        
        public RenderContext(int screenWidth, int screenHeight, Camera camera) {
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.camera = camera;
            this.lights = new ArrayList<>();
            this.backgroundColor = Color.BLACK;
        }
        
        // Getters
        public int getScreenWidth() { return screenWidth; }
        public int getScreenHeight() { return screenHeight; }
        public Camera getCamera() { return camera; }
        public List<Light> getLights() { return new ArrayList<>(lights); }
        public Color getBackgroundColor() { return backgroundColor; }
        
        public void addLight(Light light) {
            lights.add(light);
        }
    }
    
    /**
     * Represents a light source
     */
    public static class Light {
        public enum LightType {
            POINT, DIRECTIONAL, SPOT
        }
        
        private LightType type;
        private Vector3D position;
        private Vector3D direction;
        private Color color;
        private double intensity;
        private double constantAttenuation;
        private double linearAttenuation;
        private double quadraticAttenuation;
        
        public Light(LightType type, Vector3D position, Color color, double intensity) {
            this.type = type;
            this.position = position;
            this.color = color;
            this.intensity = intensity;
            this.constantAttenuation = 1.0;
            this.linearAttenuation = 0.0;
            this.quadraticAttenuation = 0.0;
        }
        
        // Getters and setters
        public LightType getType() { return type; }
        public void setType(LightType type) { this.type = type; }
        public Vector3D getPosition() { return position; }
        public void setPosition(Vector3D position) { this.position = position; }
        public Vector3D getDirection() { return direction; }
        public void setDirection(Vector3D direction) { this.direction = direction; }
        public Color getColor() { return color; }
        public void setColor(Color color) { this.color = color; }
        public double getIntensity() { return intensity; }
        public void setIntensity(double intensity) { this.intensity = intensity; }
        public double getConstantAttenuation() { return constantAttenuation; }
        public void setConstantAttenuation(double constantAttenuation) { this.constantAttenuation = constantAttenuation; }
        public double getLinearAttenuation() { return linearAttenuation; }
        public void setLinearAttenuation(double linearAttenuation) { this.linearAttenuation = linearAttenuation; }
        public double getQuadraticAttenuation() { return quadraticAttenuation; }
        public void setQuadraticAttenuation(double quadraticAttenuation) { this.quadraticAttenuation = quadraticAttenuation; }
    }
    
    /**
     * Creates a cube mesh
     */
    public static Mesh createCube(double size) {
        Mesh mesh = new Mesh();
        
        // Define vertices
        double halfSize = size / 2.0;
        Vector3D[] positions = {
            new Vector3D(-halfSize, -halfSize, -halfSize), // 0
            new Vector3D(halfSize, -halfSize, -halfSize),  // 1
            new Vector3D(halfSize, halfSize, -halfSize),   // 2
            new Vector3D(-halfSize, halfSize, -halfSize),  // 3
            new Vector3D(-halfSize, -halfSize, halfSize),  // 4
            new Vector3D(halfSize, -halfSize, halfSize),   // 5
            new Vector3D(halfSize, halfSize, halfSize),    // 6
            new Vector3D(-halfSize, halfSize, halfSize)    // 7
        };
        
        // Define normals and colors
        Vector3D[] normals = {
            new Vector3D(0, 0, -1), // Front
            new Vector3D(0, 0, 1),  // Back
            new Vector3D(-1, 0, 0), // Left
            new Vector3D(1, 0, 0),  // Right
            new Vector3D(0, -1, 0), // Bottom
            new Vector3D(0, 1, 0)   // Top
        };
        
        Vector3D[] colors = {
            new Vector3D(1, 0, 0), // Red
            new Vector3D(0, 1, 0), // Green
            new Vector3D(0, 0, 1), // Blue
            new Vector3D(1, 1, 0), // Yellow
            new Vector3D(1, 0, 1), // Magenta
            new Vector3D(0, 1, 1)  // Cyan
        };
        
        // Add vertices
        for (int i = 0; i < positions.length; i++) {
            mesh.addVertex(new Vertex(positions[i], normals[i / 4], colors[i / 4]));
        }
        
        // Define triangles (front, back, left, right, bottom, top)
        int[][] triangleIndices = {
            {0, 1, 2}, {2, 3, 0}, // Front face
            {5, 4, 7}, {7, 6, 5}, // Back face
            {4, 0, 3}, {3, 7, 4}, // Left face
            {1, 5, 6}, {6, 2, 1}, // Right face
            {4, 5, 1}, {1, 0, 4}, // Bottom face
            {3, 2, 6}, {6, 7, 3}  // Top face
        };
        
        // Add triangles
        for (int[] indices : triangleIndices) {
            mesh.addTriangle(indices[0], indices[1], indices[2]);
        }
        
        return mesh;
    }
    
    /**
     * Creates a sphere mesh
     */
    public static Mesh createSphere(double radius, int segments) {
        Mesh mesh = new Mesh();
        
        // Generate vertices
        for (int i = 0; i <= segments; i++) {
            double phi = Math.PI * i / segments;
            for (int j = 0; j <= segments; j++) {
                double theta = 2 * Math.PI * j / segments;
                
                double x = radius * Math.sin(phi) * Math.cos(theta);
                double y = radius * Math.cos(phi);
                double z = radius * Math.sin(phi) * Math.sin(theta);
                
                Vector3D position = new Vector3D(x, y, z);
                Vector3D normal = position.normalize();
                Vector3D color = new Vector3D(1, 1, 1); // White
                
                mesh.addVertex(new Vertex(position, normal, color));
            }
        }
        
        // Generate triangles
        for (int i = 0; i < segments; i++) {
            for (int j = 0; j < segments; j++) {
                int first = i * (segments + 1) + j;
                int second = first + segments + 1;
                
                // First triangle
                mesh.addTriangle(first, second, first + 1);
                
                // Second triangle
                mesh.addTriangle(second, second + 1, first + 1);
            }
        }
        
        return mesh;
    }
    
    /**
     * Rasterizes a triangle to screen pixels
     */
    public static List<Vector3D> rasterizeTriangle(Triangle triangle, int screenWidth, int screenHeight) {
        List<Vector3D> pixels = new ArrayList<>();
        
        // Convert normalized device coordinates to screen coordinates
        Vector3D screenV1 = ndcToScreen(triangle.v1.position, screenWidth, screenHeight);
        Vector3D screenV2 = ndcToScreen(triangle.v2.position, screenWidth, screenHeight);
        Vector3D screenV3 = ndcToScreen(triangle.v3.position, screenWidth, screenHeight);
        
        // fill the triangle with pixels using a simple scanline algorithm
        // this is a simplified implementation - in a real renderer, you would use
        // more sophisticated algorithms like barycentric coordinates or edge walking
        pixels.add(screenV1);
        pixels.add(screenV2);
        pixels.add(screenV3);
        
        return pixels;
    }
    
    /**
     * Convert normalized device coordinates to screen coordinates
     */
    private static Vector3D ndcToScreen(Vector3D ndc, int screenWidth, int screenHeight) {
        double x = (ndc.getX() + 1.0) * 0.5 * screenWidth;
        double y = (1.0 - ndc.getY()) * 0.5 * screenHeight;
        double z = (ndc.getZ() + 1.0) * 0.5;
        return new Vector3D(x, y, z);
    }
    
    /**
     * Create a procedural terrain mesh
     */
    public static Mesh createTerrain(int width, int depth, double scale, java.util.function.Function<Vector3D, Double> heightFunction) {
        Mesh mesh = new Mesh();
        
        // Generate vertices
        for (int z = 0; z < depth; z++) {
            for (int x = 0; x < width; x++) {
                double worldX = (x - width / 2.0) * scale;
                double worldZ = (z - depth / 2.0) * scale;
                Vector3D position = new Vector3D(worldX, 0, worldZ);
                
                // Calculate height using the provided function
                double height = heightFunction.apply(position);
                position = new Vector3D(position.getX(), height, position.getZ());
                
                // Calculate normal using finite differences
                double eps = 0.01;
                double hLeft = heightFunction.apply(new Vector3D(worldX - eps, 0, worldZ));
                double hRight = heightFunction.apply(new Vector3D(worldX + eps, 0, worldZ));
                double hBack = heightFunction.apply(new Vector3D(worldX, 0, worldZ - eps));
                double hFront = heightFunction.apply(new Vector3D(worldX, 0, worldZ + eps));
                
                Vector3D normal = new Vector3D(
                    hLeft - hRight,
                    2 * eps,
                    hBack - hFront
                ).normalize();
                
                Vector3D color = new Vector3D(0.5, 0.5, 0.5);
                
                mesh.addVertex(new Vertex(position, normal, color));
            }
        }
        
        // Generate triangles
        for (int z = 0; z < depth - 1; z++) {
            for (int x = 0; x < width - 1; x++) {
                int topLeft = z * width + x;
                int topRight = topLeft + 1;
                int bottomLeft = (z + 1) * width + x;
                int bottomRight = bottomLeft + 1;
                
                // First triangle
                mesh.addTriangle(topLeft, bottomLeft, topRight);
                
                // Second triangle
                mesh.addTriangle(topRight, bottomLeft, bottomRight);
            }
        }
        
        return mesh;
    }
    
    /**
     * Create a wave height function for terrain
     */
    public static java.util.function.Function<Vector3D, Double> createWaveHeightFunction(double amplitude, double frequency) {
        return position -> amplitude * Math.sin(frequency * position.getX()) * Math.cos(frequency * position.getZ());
    }
    
    /**
     * Create a fractal height function for terrain
     */
    public static java.util.function.Function<Vector3D, Double> createFractalHeightFunction(int octaves, double persistence, double scale) {
        return position -> {
            double value = 0;
            double amplitude = 1;
            double frequency = scale;
            
            for (int i = 0; i < octaves; i++) {
                value += amplitude * Math.sin(frequency * position.getX()) * Math.cos(frequency * position.getZ());
                amplitude *= persistence;
                frequency *= 2;
            }
            
            return value;
        };
    }
    
    /**
     * Merge multiple meshes into one
     */
    public static Mesh mergeMeshes(List<Mesh> meshes) {
        Mesh result = new Mesh();
        
        for (Mesh mesh : meshes) {
            int vertexOffset = result.vertices.size();
            
            // Add vertices
            for (Vertex vertex : mesh.vertices) {
                result.addVertex(new Vertex(
                    new Vector3D(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ()),
                    new Vector3D(vertex.normal.getX(), vertex.normal.getY(), vertex.normal.getZ()),
                    new Vector3D(vertex.color.getX(), vertex.color.getY(), vertex.color.getZ())
                ));
            }
            
            // Add triangles with offset
            for (Triangle triangle : mesh.triangles) {
                Vertex v1 = result.vertices.get(vertexOffset + mesh.vertices.indexOf(triangle.v1));
                Vertex v2 = result.vertices.get(vertexOffset + mesh.vertices.indexOf(triangle.v2));
                Vertex v3 = result.vertices.get(vertexOffset + mesh.vertices.indexOf(triangle.v3));
                result.addTriangle(new Triangle(v1, v2, v3));
            }
        }
        
        return result;
    }
    
    /**
     * Subdivide mesh for smoother surfaces
     */
    public static Mesh subdivideMesh(Mesh mesh) {
        Mesh result = new Mesh();
        
        // For each triangle, create 4 new triangles
        for (Triangle triangle : mesh.triangles) {
            // Calculate midpoints
            Vector3D mid1 = triangle.v1.position.add(triangle.v2.position).divide(2);
            Vector3D mid2 = triangle.v2.position.add(triangle.v3.position).divide(2);
            Vector3D mid3 = triangle.v3.position.add(triangle.v1.position).divide(2);
            
            // Create new vertices
            Vertex newV1 = new Vertex(mid1, triangle.v1.normal.add(triangle.v2.normal).divide(2).normalize(), triangle.v1.color.add(triangle.v2.color).divide(2));
            Vertex newV2 = new Vertex(mid2, triangle.v2.normal.add(triangle.v3.normal).divide(2).normalize(), triangle.v2.color.add(triangle.v3.color).divide(2));
            Vertex newV3 = new Vertex(mid3, triangle.v3.normal.add(triangle.v1.normal).divide(2).normalize(), triangle.v3.color.add(triangle.v1.color).divide(2));
            
            // Add vertices
            result.addVertex(new Vertex(
                new Vector3D(triangle.v1.position.getX(), triangle.v1.position.getY(), triangle.v1.position.getZ()),
                new Vector3D(triangle.v1.normal.getX(), triangle.v1.normal.getY(), triangle.v1.normal.getZ()),
                new Vector3D(triangle.v1.color.getX(), triangle.v1.color.getY(), triangle.v1.color.getZ())
            ));
            result.addVertex(new Vertex(
                new Vector3D(triangle.v2.position.getX(), triangle.v2.position.getY(), triangle.v2.position.getZ()),
                new Vector3D(triangle.v2.normal.getX(), triangle.v2.normal.getY(), triangle.v2.normal.getZ()),
                new Vector3D(triangle.v2.color.getX(), triangle.v2.color.getY(), triangle.v2.color.getZ())
            ));
            result.addVertex(new Vertex(
                new Vector3D(triangle.v3.position.getX(), triangle.v3.position.getY(), triangle.v3.position.getZ()),
                new Vector3D(triangle.v3.normal.getX(), triangle.v3.normal.getY(), triangle.v3.normal.getZ()),
                new Vector3D(triangle.v3.color.getX(), triangle.v3.color.getY(), triangle.v3.color.getZ())
            ));
            result.addVertex(newV1);
            result.addVertex(newV2);
            result.addVertex(newV3);
            
            int baseIndex = result.vertices.size() - 6;
            
            // Add 4 new triangles
            result.addTriangle(
                new Triangle(
                    result.vertices.get(baseIndex),
                    result.vertices.get(baseIndex + 3),
                    result.vertices.get(baseIndex + 5)
                )
            );
            result.addTriangle(
                new Triangle(
                    result.vertices.get(baseIndex + 1),
                    result.vertices.get(baseIndex + 4),
                    result.vertices.get(baseIndex + 3)
                )
            );
            result.addTriangle(
                new Triangle(
                    result.vertices.get(baseIndex + 2),
                    result.vertices.get(baseIndex + 5),
                    result.vertices.get(baseIndex + 4)
                )
            );
            result.addTriangle(
                new Triangle(
                    result.vertices.get(baseIndex + 3),
                    result.vertices.get(baseIndex + 4),
                    result.vertices.get(baseIndex + 5)
                )
            );
        }
        
        return result;
    }
}