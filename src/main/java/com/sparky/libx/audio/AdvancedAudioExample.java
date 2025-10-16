package com.sparky.libx.audio;

import com.sparky.libx.audio.AudioEngine.Engine;
import com.sparky.libx.audio.AudioEngine.WaveForm;
import com.sparky.libx.audio.AudioEngine.AudioEffect;
import com.sparky.libx.math.Vector3D;

/**
 * Advanced example demonstrating the full capabilities of the SparkyLibX Audio Engine
 * This example shows how to use 3D audio, effects, synthesis, and mixing
 */
public class AdvancedAudioExample {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== SparkyLibX Advanced Audio Engine Demo ===");
            
            // Initialize the audio engine
            Engine audioEngine = new Engine();
            audioEngine.initialize();
            
            // Generate procedural sounds
            System.out.println("1. Generating procedural sounds...");
            AudioEngine.AudioClip sineWave = audioEngine.generateSound(
                "sine_440hz", WaveForm.SINE, 440.0, 2.0); // A4 note
            AudioEngine.AudioClip squareWave = audioEngine.generateSound(
                "square_220hz", WaveForm.SQUARE, 220.0, 2.0); // A3 note
            AudioEngine.AudioClip sawtoothWave = audioEngine.generateSound(
                "sawtooth_110hz", WaveForm.SAWTOOTH, 110.0, 2.0); // A2 note
            AudioEngine.AudioClip whiteNoise = audioEngine.generateSound(
                "noise", WaveForm.NOISE, 0, 1.0);
            
            // Store clips in the engine
            // Note: In a real implementation, we would load actual audio files
            System.out.println("2. Storing audio clips in engine...");
            
            // Apply effects to clips
            System.out.println("3. Applying audio effects...");
            AudioEngine.AudioClip echoSine = audioEngine.applyEffect(
                sineWave, AudioEffect.ECHO, 0.3, 0.7); // 300ms delay, 70% decay
            AudioEngine.AudioClip distortedSquare = audioEngine.applyEffect(
                squareWave, AudioEffect.DISTORTION, 2.0, 0.8); // 2x gain, 0.8 threshold
            AudioEngine.AudioClip chorusedSawtooth = audioEngine.applyEffect(
                sawtoothWave, AudioEffect.CHORUS, 0.5, 2.0); // 0.5 depth, 2.0 rate
            
            // Create audio sources
            System.out.println("4. Creating audio sources...");
            AudioEngine.AudioSource source1 = audioEngine.createSource("sine_440hz");
            source1.setPosition(new Vector3D(-5, 0, 0)); // Left of listener
            source1.setVolume(0.8);
            
            AudioEngine.AudioSource source2 = audioEngine.createSource("square_220hz");
            source2.setPosition(new Vector3D(5, 0, 0)); // Right of listener
            source2.setVolume(0.7);
            
            AudioEngine.AudioSource source3 = audioEngine.createSource("sawtooth_110hz");
            source3.setPosition(new Vector3D(0, 0, 5)); // Behind listener
            source3.setVolume(0.6);
            
            // Set up the listener
            System.out.println("5. Setting up audio listener...");
            AudioEngine.AudioListener listener = audioEngine.getListener();
            listener.setPosition(new Vector3D(0, 0, 0)); // At origin
            listener.setForward(new Vector3D(0, 0, 1)); // Facing positive Z
            listener.setUp(new Vector3D(0, 1, 0)); // Up is positive Y
            
            // Configure mixer
            System.out.println("6. Configuring audio mixer...");
            AudioEngine.AudioMixer mixer = audioEngine.getMixer();
            mixer.setMasterVolume(0.9);
            
            // Add a reverb effect
            AudioEngine.ReverbParameters reverb = new AudioEngine.ReverbParameters(0.7, 0.5, 1.0, 0.4);
            mixer.setReverb(reverb);
            
            // Add a low-pass filter
            AudioEngine.AudioFilter lowPass = new AudioEngine.AudioFilter(
                AudioEngine.AudioFilter.FilterType.LOW_PASS, 1000, 0.7, 0);
            mixer.addFilter(lowPass);
            
            // Play sounds
            System.out.println("7. Playing sounds...");
            source1.play();
            source2.play();
            source3.play();
            
            // Simulate movement
            System.out.println("8. Simulating 3D audio movement...");
            for (int i = 0; i < 10; i++) {
                // Move source1 in a circle around the listener
                double angle = i * Math.PI / 5;
                Vector3D newPosition = new Vector3D(
                    3 * Math.cos(angle),
                    0,
                    3 * Math.sin(angle)
                );
                source1.setPosition(newPosition);
                
                // Update the engine
                audioEngine.update(0.1); // 100ms delta time
                
                // Print status
                System.out.printf("Frame %d: Source1 at (%.2f, %.2f, %.2f)%n",
                    i, newPosition.getX(), newPosition.getY(), newPosition.getZ());
                
                // Small delay to simulate real-time
                Thread.sleep(100);
            }
            
            // Stop all sources
            System.out.println("9. Stopping all audio sources...");
            source1.stop();
            source2.stop();
            source3.stop();
            
            // Print final statistics
            System.out.println("10. Audio Engine Statistics:");
            System.out.println("   - Total sources created: " + audioEngine.getAllSources().size());
            System.out.println("   - Master volume: " + audioEngine.getMasterVolume());
            System.out.println("   - Active sources: " + audioEngine.getAllSources().stream()
                .filter(s -> s.isPlaying() && !s.isPaused()).count());
            
            // Shutdown the engine
            System.out.println("11. Shutting down audio engine...");
            audioEngine.shutdown();
            
            System.out.println("=== Demo completed successfully! ===");
            
        } catch (Exception e) {
            System.err.println("Error in audio demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}