package com.sparky.libx.graphics;

import com.sparky.libx.math.Vector3D;
import com.sparky.libx.math.Matrix4x4;
import com.sparky.libx.math.Quaternion;

import java.util.*;
import java.util.List;

/**
 * Advanced 3D Graphics Rendering Engine for Minecraft Plugins
 * Provides capabilities for 3D transformations, lighting, and rendering
 */
public class Renderer3D {
    
    /**
     * Represents a 3D point with position and color
     */
    public static class Vertex {
        public Vector3D position;
        public Vector3D normal;
        public Vector3D color;
        public Vector3D texCoord;
        
        public Vertex(Vector3D position) {
            this.position = position;
            this.normal = new Vector3D(0, 0, 0);
            this.color = new Vector3D(1, 1, 1);
            this.texCoord = new Vector3D(0, 0, 0);
        }
        
        public Vertex(Vector3D position, Vector3D normal, Vector3D color) {
            this.position = position;
            this.normal = normal;
            this.color = color;
            this.texCoord = new Vector3D(0, 0, 0);
        }
        
        public Vertex(Vector3D position, Vector3D normal, Vector3D color, Vector3D texCoord) {
            this.position = position;
            this.normal = normal;
            this.color = color;
            this.texCoord = texCoord;
        }
        
        public Vertex copy() {
            return new Vertex(
                new Vector3D(position.getX(), position.getY(), position.getZ()),
                new Vector3D(normal.getX(), normal.getY(), normal.getZ()),
                new Vector3D(color.getX(), color.getY(), color.getZ()),
                new Vector3D(texCoord.getX(), texCoord.getY(), texCoord.getZ())
            );
        }
        
        @Override
        public String toString() {
            return String.format("Vertex{pos=%s, norm=%s, col=%s}", position, normal, color);
        }
    }
    
    /**
     * Represents a triangle face with three vertices
     */
    public static class Triangle {
        public Vertex v1, v2, v3;
        public Vector3D normal;
        
        public Triangle(Vertex v1, Vertex v2, Vertex v3) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.normal = calculateNormal();
        }
        
        public Vector3D calculateNormal() {
            Vector3D edge1 = v2.position.subtract(v1.position);
            Vector3D edge2 = v3.position.subtract(v1.position);
            return edge1.cross(edge2).normalize();
        }
        
        public Triangle copy() {
            return new Triangle(v1.copy(), v2.copy(), v3.copy());
        }
        
        @Override
        public String toString() {
            return String.format("Triangle{v1=%s, v2=%s, v3=%s}", v1, v2, v3);
        }
    }
    
    /**
     * Represents a 3D mesh composed of vertices and triangles
     */
    public static class Mesh {
        public List<Vertex> vertices;
        public List<Triangle> triangles;
        public Vector3D position;
        public Vector3D rotation;
        public Vector3D scale;
        
        public Mesh() {
            this.vertices = new ArrayList<>();
            this.triangles = new ArrayList<>();
            this.position = new Vector3D(0, 0, 0);
            this.rotation = new Vector3D(0, 0, 0);
            this.scale = new Vector3D(1, 1, 1);
        }
        
        public Mesh(List<Vertex> vertices, List<Triangle> triangles) {
            this.vertices = new ArrayList<>(vertices);
            this.triangles = new ArrayList<>(triangles);
            this.position = new Vector3D(0, 0, 0);
            this.rotation = new Vector3D(0, 0, 0);
            this.scale = new Vector3D(1, 1, 1);
        }
        
        public void addVertex(Vertex vertex) {
            vertices.add(vertex);
        }
        
        public void addTriangle(Triangle triangle) {
            triangles.add(triangle);
        }
        
        public void addTriangle(int v1, int v2, int v3) {
            if (v1 < vertices.size() && v2 < vertices.size() && v3 < vertices.size()) {
                triangles.add(new Triangle(vertices.get(v1), vertices.get(v2), vertices.get(v3)));
            }
        }
        
        public Mesh copy() {
            Mesh copy = new Mesh();
            for (Vertex v : vertices) {
                copy.addVertex(v.copy());
            }
            for (Triangle t : triangles) {
                copy.addTriangle(t.copy());
            }
            copy.position = new Vector3D(position.getX(), position.getY(), position.getZ());
            copy.rotation = new Vector3D(rotation.getX(), rotation.getY(), rotation.getZ());
            copy.scale = new Vector3D(scale.getX(), scale.getY(), scale.getZ());
            return copy;
        }
        
        @Override
        public String toString() {
            return String.format("Mesh{vertices=%d, triangles=%d}", vertices.size(), triangles.size());
        }
    }
    
    /**
     * Represents a camera with position, orientation, and projection parameters
     */
    public static class Camera {
        public Vector3D position;
        public Vector3D target;
        public Vector3D up;
        public double fov;
        public double aspectRatio;
        public double nearPlane;
        public double farPlane;
        
        public Camera() {
            this.position = new Vector3D(0, 0, 0);
            this.target = new Vector3D(0, 0, 1);
            this.up = new Vector3D(0, 1, 0);
            this.fov = Math.toRadians(60);
            this.aspectRatio = 16.0 / 9.0;
            this.nearPlane = 0.1;
            this.farPlane = 1000.0;
        }
        
        public Camera(Vector3D position, Vector3D target, Vector3D up, double fov, double aspectRatio, double nearPlane, double farPlane) {
            this.position = position;
            this.target = target;
            this.up = up;
            this.fov = fov;
            this.aspectRatio = aspectRatio;
            this.nearPlane = nearPlane;
            this.farPlane = farPlane;
        }
        
        public Matrix4x4 getViewMatrix() {
            Vector3D forward = target.subtract(position).normalize();
            Vector3D right = forward.cross(up).normalize();
            Vector3D newUp = right.cross(forward);
            
            Matrix4x4 view = new Matrix4x4();
            view.set(0, 0, right.getX());
            view.set(0, 1, right.getY());
            view.set(0, 2, right.getZ());
            view.set(1, 0, newUp.getX());
            view.set(1, 1, newUp.getY());
            view.set(1, 2, newUp.getZ());
            view.set(2, 0, -forward.getX());
            view.set(2, 1, -forward.getY());
            view.set(2, 2, -forward.getZ());
            view.set(3, 0, -right.dot(position));
            view.set(3, 1, -newUp.dot(position));
            view.set(3, 2, forward.dot(position));
            view.set(3, 3, 1);
            
            return view;
        }
        
        public Matrix4x4 getProjectionMatrix() {
            double f = 1.0 / Math.tan(fov / 2.0);
            double rangeInv = 1.0 / (nearPlane - farPlane);
            
            Matrix4x4 proj = new Matrix4x4();
            proj.set(0, 0, f / aspectRatio);
            proj.set(1, 1, f);
            proj.set(2, 2, (nearPlane + farPlane) * rangeInv);
            proj.set(2, 3, -1);
            proj.set(3, 2, 2.0 * nearPlane * farPlane * rangeInv);
            proj.set(3, 3, 0);
            
            return proj;
        }
    }
    
    /**
     * Represents a light source with position, color, and intensity
     */
    public static class Light {
        public enum LightType {
            DIRECTIONAL, POINT, SPOT
        }
        
        public LightType type;
        public Vector3D position;
        public Vector3D direction;
        public Vector3D color;
        public double intensity;
        public double constant;
        public double linear;
        public double quadratic;
        public double cutOff;
        public double outerCutOff;
        
        public Light(LightType type, Vector3D position, Vector3D color, double intensity) {
            this.type = type;
            this.position = position;
            this.direction = new Vector3D(0, -1, 0);
            this.color = color;
            this.intensity = intensity;
            this.constant = 1.0;
            this.linear = 0.09;
            this.quadratic = 0.032;
            this.cutOff = Math.cos(Math.toRadians(12.5));
            this.outerCutOff = Math.cos(Math.toRadians(15.0));
        }
    }
    
    /**
     * Represents material properties for lighting calculations
     */
    public static class Material {
        public Vector3D ambient;
        public Vector3D diffuse;
        public Vector3D specular;
        public double shininess;
        
        public Material(Vector3D ambient, Vector3D diffuse, Vector3D specular, double shininess) {
            this.ambient = ambient;
            this.diffuse = diffuse;
            this.specular = specular;
            this.shininess = shininess;
        }
    }
    
    /**
     * Rendering context with transformation matrices and rendering parameters
     */
    public static class RenderContext {
        public Matrix4x4 modelMatrix;
        public Matrix4x4 viewMatrix;
        public Matrix4x4 projectionMatrix;
        public List<Light> lights;
        public Material material;
        public Vector3D cameraPosition;
        
        public RenderContext() {
            this.modelMatrix = new Matrix4x4();
            this.viewMatrix = new Matrix4x4();
            this.projectionMatrix = new Matrix4x4();
            this.lights = new ArrayList<>();
            this.material = new Material(
                new Vector3D(0.1, 0.1, 0.1),
                new Vector3D(0.8, 0.8, 0.8),
                new Vector3D(1.0, 1.0, 1.0),
                32.0
            );
            this.cameraPosition = new Vector3D(0, 0, 0);
        }
    }
    
    /**
     * Create a cube mesh
     */
    public static Mesh createCube(double size) {
        Mesh mesh = new Mesh();
        
        // Define vertices
        double half = size / 2.0;
        Vertex[] cubeVertices = {
            // Front face
            new Vertex(new Vector3D(-half, -half, half)),
            new Vertex(new Vector3D(half, -half, half)),
            new Vertex(new Vector3D(half, half, half)),
            new Vertex(new Vector3D(-half, half, half)),
            // Back face
            new Vertex(new Vector3D(half, -half, -half)),
            new Vertex(new Vector3D(-half, -half, -half)),
            new Vertex(new Vector3D(-half, half, -half)),
            new Vertex(new Vector3D(half, half, -half)),
            // Top face
            new Vertex(new Vector3D(-half, half, half)),
            new Vertex(new Vector3D(half, half, half)),
            new Vertex(new Vector3D(half, half, -half)),
            new Vertex(new Vector3D(-half, half, -half)),
            // Bottom face
            new Vertex(new Vector3D(-half, -half, -half)),
            new Vertex(new Vector3D(half, -half, -half)),
            new Vertex(new Vector3D(half, -half, half)),
            new Vertex(new Vector3D(-half, -half, half)),
            // Right face
            new Vertex(new Vector3D(half, -half, half)),
            new Vertex(new Vector3D(half, -half, -half)),
            new Vertex(new Vector3D(half, half, -half)),
            new Vertex(new Vector3D(half, half, half)),
            // Left face
            new Vertex(new Vector3D(-half, -half, -half)),
            new Vertex(new Vector3D(-half, -half, half)),
            new Vertex(new Vector3D(-half, half, half)),
            new Vertex(new Vector3D(-half, half, -half))
        };
        
        // Add vertices to mesh
        for (Vertex v : cubeVertices) {
            mesh.addVertex(v);
        }
        
        // Define triangles (two per face)
        int[][] indices = {
            // Front
            {0, 1, 2}, {2, 3, 0},
            // Back
            {4, 5, 6}, {6, 7, 4},
            // Top
            {8, 9, 10}, {10, 11, 8},
            // Bottom
            {12, 13, 14}, {14, 15, 12},
            // Right
            {16, 17, 18}, {18, 19, 16},
            // Left
            {20, 21, 22}, {22, 23, 20}
        };
        
        for (int[] tri : indices) {
            mesh.addTriangle(tri[0], tri[1], tri[2]);
        }
        
        return mesh;
    }
    
    /**
     * Create a sphere mesh
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
                Vector3D color = new Vector3D(1, 1, 1);
                
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
     * Create a torus mesh
     */
    public static Mesh createTorus(double majorRadius, double minorRadius, int majorSegments, int minorSegments) {
        Mesh mesh = new Mesh();
        
        // Generate vertices
        for (int i = 0; i <= majorSegments; i++) {
            double majorAngle = 2 * Math.PI * i / majorSegments;
            
            for (int j = 0; j <= minorSegments; j++) {
                double minorAngle = 2 * Math.PI * j / minorSegments;
                
                double x = (majorRadius + minorRadius * Math.cos(minorAngle)) * Math.cos(majorAngle);
                double y = minorRadius * Math.sin(minorAngle);
                double z = (majorRadius + minorRadius * Math.cos(minorAngle)) * Math.sin(majorAngle);
                
                Vector3D position = new Vector3D(x, y, z);
                
                // Calculate normal
                double nx = Math.cos(minorAngle) * Math.cos(majorAngle);
                double ny = Math.sin(minorAngle);
                double nz = Math.cos(minorAngle) * Math.sin(majorAngle);
                Vector3D normal = new Vector3D(nx, ny, nz).normalize();
                
                Vector3D color = new Vector3D(1, 1, 1);
                
                mesh.addVertex(new Vertex(position, normal, color));
            }
        }
        
        // Generate triangles
        for (int i = 0; i < majorSegments; i++) {
            for (int j = 0; j < minorSegments; j++) {
                int first = i * (minorSegments + 1) + j;
                int second = first + minorSegments + 1;
                
                // First triangle
                mesh.addTriangle(first, second, first + 1);
                
                // Second triangle
                mesh.addTriangle(second, second + 1, first + 1);
            }
        }
        
        return mesh;
    }
    
    /**
     * Apply transformation to a mesh
     */
    public static Mesh transformMesh(Mesh mesh, Matrix4x4 transformation) {
        Mesh result = mesh.copy();
        
        for (Vertex vertex : result.vertices) {
            vertex.position = multiplyMatrixVector(transformation, vertex.position);
        }
        
        // Recalculate normals
        for (Triangle triangle : result.triangles) {
            triangle.normal = triangle.calculateNormal();
        }
        
        return result;
    }
    
    /**
     * Multiply matrix by vector
     */
    private static Vector3D multiplyMatrixVector(Matrix4x4 matrix, Vector3D vector) {
        double x = matrix.get(0, 0) * vector.getX() + matrix.get(0, 1) * vector.getY() + matrix.get(0, 2) * vector.getZ() + matrix.get(0, 3);
        double y = matrix.get(1, 0) * vector.getX() + matrix.get(1, 1) * vector.getY() + matrix.get(1, 2) * vector.getZ() + matrix.get(1, 3);
        double z = matrix.get(2, 0) * vector.getX() + matrix.get(2, 1) * vector.getY() + matrix.get(2, 2) * vector.getZ() + matrix.get(2, 3);
        // For 3D vectors, we ignore the w component or assume it's 1
        return new Vector3D(x, y, z);
    }
    
    /**
     * Apply translation to a mesh
     */
    public static Mesh translateMesh(Mesh mesh, Vector3D translation) {
        Matrix4x4 transform = createTranslationMatrix(translation);
        return transformMesh(mesh, transform);
    }
    
    /**
     * Create translation matrix
     */
    private static Matrix4x4 createTranslationMatrix(Vector3D translation) {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.set(0, 3, translation.getX());
        matrix.set(1, 3, translation.getY());
        matrix.set(2, 3, translation.getZ());
        return matrix;
    }
    
    /**
     * Apply rotation to a mesh
     */
    public static Mesh rotateMesh(Mesh mesh, Vector3D rotation) {
        Matrix4x4 transform = createRotationMatrix(rotation);
        return transformMesh(mesh, transform);
    }
    
    /**
     * Create rotation matrix from Euler angles
     */
    private static Matrix4x4 createRotationMatrix(Vector3D rotation) {
        Matrix4x4 rx = createRotationXMatrix(rotation.getX());
        Matrix4x4 ry = createRotationYMatrix(rotation.getY());
        Matrix4x4 rz = createRotationZMatrix(rotation.getZ());
        
        return rz.multiply(ry).multiply(rx);
    }
    
    /**
     * Create rotation matrix around X axis
     */
    private static Matrix4x4 createRotationXMatrix(double angle) {
        Matrix4x4 matrix = new Matrix4x4();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        matrix.set(1, 1, cos);
        matrix.set(1, 2, -sin);
        matrix.set(2, 1, sin);
        matrix.set(2, 2, cos);
        return matrix;
    }
    
    /**
     * Create rotation matrix around Y axis
     */
    private static Matrix4x4 createRotationYMatrix(double angle) {
        Matrix4x4 matrix = new Matrix4x4();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        matrix.set(0, 0, cos);
        matrix.set(0, 2, sin);
        matrix.set(2, 0, -sin);
        matrix.set(2, 2, cos);
        return matrix;
    }
    
    /**
     * Create rotation matrix around Z axis
     */
    private static Matrix4x4 createRotationZMatrix(double angle) {
        Matrix4x4 matrix = new Matrix4x4();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        matrix.set(0, 0, cos);
        matrix.set(0, 1, -sin);
        matrix.set(1, 0, sin);
        matrix.set(1, 1, cos);
        return matrix;
    }
    
    /**
     * Apply scaling to a mesh
     */
    public static Mesh scaleMesh(Mesh mesh, Vector3D scale) {
        Matrix4x4 transform = createScaleMatrix(scale);
        return transformMesh(mesh, transform);
    }
    
    /**
     * Create scale matrix
     */
    private static Matrix4x4 createScaleMatrix(Vector3D scale) {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.set(0, 0, scale.getX());
        matrix.set(1, 1, scale.getY());
        matrix.set(2, 2, scale.getZ());
        return matrix;
    }
    
    /**
     * Calculate lighting for a vertex
     */
    public static Vector3D calculateLighting(Vertex vertex, RenderContext context) {
        Vector3D result = new Vector3D(0, 0, 0);
        
        // Ambient lighting
        Vector3D ambient = multiplyVectors(context.material.ambient, vertex.color);
        result = result.add(ambient);
        
        for (Light light : context.lights) {
            Vector3D lightContribution = new Vector3D(0, 0, 0);
            
            if (light.type == Light.LightType.DIRECTIONAL) {
                // Directional light
                Vector3D lightDir = light.direction.normalize().multiply(-1);
                double diff = Math.max(vertex.normal.dot(lightDir), 0.0);
                Vector3D diffuse = multiplyVectors(context.material.diffuse, light.color).multiply(diff);
                
                // Specular
                Vector3D viewDir = context.cameraPosition.subtract(vertex.position).normalize();
                Vector3D reflectDir = reflectVector(lightDir.multiply(-1), vertex.normal);
                double spec = Math.pow(Math.max(viewDir.dot(reflectDir), 0.0), context.material.shininess);
                Vector3D specular = multiplyVectors(context.material.specular, light.color).multiply(spec);
                
                lightContribution = diffuse.add(specular);
            } else if (light.type == Light.LightType.POINT) {
                // Point light
                Vector3D lightDir = light.position.subtract(vertex.position);
                double distance = lightDir.magnitude();
                lightDir = lightDir.normalize();
                
                double diff = Math.max(vertex.normal.dot(lightDir), 0.0);
                Vector3D diffuse = multiplyVectors(context.material.diffuse, light.color).multiply(diff);
                
                // Attenuation
                double attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * distance * distance);
                diffuse = diffuse.multiply(attenuation);
                
                // Specular
                Vector3D viewDir = context.cameraPosition.subtract(vertex.position).normalize();
                Vector3D reflectDir = reflectVector(lightDir.multiply(-1), vertex.normal);
                double spec = Math.pow(Math.max(viewDir.dot(reflectDir), 0.0), context.material.shininess);
                Vector3D specular = multiplyVectors(context.material.specular, light.color).multiply(spec).multiply(attenuation);
                
                lightContribution = diffuse.add(specular);
            } else if (light.type == Light.LightType.SPOT) {
                // Spot light
                Vector3D lightDir = light.position.subtract(vertex.position);
                double distance = lightDir.magnitude();
                lightDir = lightDir.normalize();
                
                // Spotlight intensity
                double theta = lightDir.multiply(-1).dot(light.direction.normalize());
                double epsilon = light.cutOff - light.outerCutOff;
                double intensity = Math.max(0.0, (theta - light.outerCutOff) / epsilon);
                
                if (theta > light.outerCutOff) {
                    double diff = Math.max(vertex.normal.dot(lightDir), 0.0);
                    Vector3D diffuse = multiplyVectors(context.material.diffuse, light.color).multiply(diff).multiply(intensity);
                    
                    // Attenuation
                    double attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * distance * distance);
                    diffuse = diffuse.multiply(attenuation);
                    
                    // Specular
                    Vector3D viewDir = context.cameraPosition.subtract(vertex.position).normalize();
                    Vector3D reflectDir = reflectVector(lightDir.multiply(-1), vertex.normal);
                    double spec = Math.pow(Math.max(viewDir.dot(reflectDir), 0.0), context.material.shininess);
                    Vector3D specular = multiplyVectors(context.material.specular, light.color).multiply(spec).multiply(intensity).multiply(attenuation);
                    
                    lightContribution = diffuse.add(specular);
                }
            }
            
            result = result.add(lightContribution.multiply(light.intensity));
        }
        
        return result;
    }
    
    /**
     * Multiply two vectors component-wise
     */
    private static Vector3D multiplyVectors(Vector3D v1, Vector3D v2) {
        return new Vector3D(
            v1.getX() * v2.getX(),
            v1.getY() * v2.getY(),
            v1.getZ() * v2.getZ()
        );
    }
    
    /**
     * Reflect a vector across a normal
     */
    private static Vector3D reflectVector(Vector3D incident, Vector3D normal) {
        double dot = incident.dot(normal);
        return incident.subtract(normal.multiply(2 * dot));
    }
    
    /**
     * Transform vertex using model-view-projection matrix
     */
    public static Vertex transformVertex(Vertex vertex, RenderContext context) {
        Vertex result = vertex.copy();
        
        // Apply model transformation
        Matrix4x4 mvp = context.projectionMatrix
            .multiply(context.viewMatrix)
            .multiply(context.modelMatrix);
        
        result.position = multiplyMatrixVector(mvp, result.position);
        
        return result;
    }
    
    /**
     * Clip triangle against near plane
     */
    public static List<Triangle> clipTriangle(Triangle triangle, double nearPlane) {
        List<Triangle> result = new ArrayList<>();
        
        // Simple clipping - just check if all vertices are in front of near plane
        if (triangle.v1.position.getZ() >= nearPlane && 
            triangle.v2.position.getZ() >= nearPlane && 
            triangle.v3.position.getZ() >= nearPlane) {
            result.add(triangle);
        }
        
        return result;
    }
    
    /**
     * Rasterize triangle to screen coordinates
     */
    public static List<Vector3D> rasterizeTriangle(Triangle triangle, int screenWidth, int screenHeight) {
        List<Vector3D> pixels = new ArrayList<>();
        
        // Convert normalized device coordinates to screen coordinates
        Vector3D screenV1 = ndcToScreen(triangle.v1.position, screenWidth, screenHeight);
        Vector3D screenV2 = ndcToScreen(triangle.v2.position, screenWidth, screenHeight);
        Vector3D screenV3 = ndcToScreen(triangle.v3.position, screenWidth, screenHeight);
        
        // Simple triangle rasterization (placeholder)
        // In a real implementation, this would fill the triangle with pixels
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
                
                // Calculate normal (simplified)
                Vector3D normal = new Vector3D(0, 1, 0);
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
                result.addVertex(vertex.copy());
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
            result.addVertex(triangle.v1.copy());
            result.addVertex(triangle.v2.copy());
            result.addVertex(triangle.v3.copy());
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