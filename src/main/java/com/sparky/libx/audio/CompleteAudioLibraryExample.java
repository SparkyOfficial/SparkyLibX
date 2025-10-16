package com.sparky.libx.audio;

import com.sparky.libx.audio.AudioEngine.WaveForm;
import com.sparky.libx.audio.AudioEngine.AudioEffect;
import com.sparky.libx.audio.AudioProcessor.EffectChain;
import com.sparky.libx.audio.AudioVisualizerAdvanced.AdvancedWaveformParams;
import com.sparky.libx.audio.AudioVisualizerAdvanced.Audio3DParams;
import com.sparky.libx.audio.AudioVisualizerAdvanced.SpectrogramParams;
import com.sparky.libx.audio.AudioAnalyzerAdvanced.AdvancedAnalysisResult;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Complete example demonstrating all features of the SparkyLibX Audio Library
 * This example shows how to use the full audio processing pipeline
 */
public class CompleteAudioLibraryExample {
    
    public static void main(String[] args) {
        System.out.println("=== SparkyLibX Complete Audio Library Demo ===");
        
        // Create audio processor
        AudioProcessor processor = new AudioProcessor();
        
        try {
            // 1. Generate procedural audio clips
            System.out.println("1. Generating procedural audio clips...");
            AudioEngine.AudioClip sineWave = processor.generateClip("sine_440hz", WaveForm.SINE, 440.0, 2.0);
            AudioEngine.AudioClip squareWave = processor.generateClip("square_220hz", WaveForm.SQUARE, 220.0, 2.0);
            AudioEngine.AudioClip sawtoothWave = processor.generateClip("sawtooth_110hz", WaveForm.SAWTOOTH, 110.0, 2.0);
            AudioEngine.AudioClip whiteNoise = processor.generateClip("noise", WaveForm.NOISE, 0, 1.0);
            
            // 2. Apply effects to clips
            System.out.println("2. Applying audio effects...");
            AudioEngine.AudioClip echoSine = processor.applyEffect(sineWave, AudioEffect.ECHO, 0.3, 0.7);
            AudioEngine.AudioClip distortedSquare = processor.applyEffect(squareWave, AudioEffect.DISTORTION, 2.0, 0.8);
            AudioEngine.AudioClip chorusedSawtooth = processor.applyEffect(sawtoothWave, AudioEffect.CHORUS, 0.5, 2.0);
            
            // 3. Apply multiple effects using effect chain
            System.out.println("3. Applying effect chains...");
            AudioEngine.AudioClip multiEffected = processor.applyEffects(
                sineWave,
                new EffectChain(AudioEffect.ECHO, 0.2, 0.6),
                new EffectChain(AudioEffect.CHORUS, 0.3, 1.5),
                new EffectChain(AudioEffect.DISTORTION, 1.5, 0.7)
            );
            
            // 4. Mix clips together
            System.out.println("4. Mixing audio clips...");
            AudioEngine.AudioClip mixedClip = processor.mixClips("mixed", sineWave, squareWave, sawtoothWave);
            
            // 5. Analyze audio clips
            System.out.println("5. Analyzing audio clips...");
            AdvancedAnalysisResult analysis = processor.analyze(sineWave);
            System.out.println("   Analysis results:");
            System.out.println("   - Duration: " + analysis.duration + " seconds");
            System.out.println("   - Average amplitude: " + analysis.averageAmplitude);
            System.out.println("   - Max amplitude: " + analysis.maxAmplitude);
            System.out.println("   - RMS: " + analysis.rms);
            System.out.println("   - Tempo: " + analysis.tempo + " BPM");
            System.out.println("   - Loudness: " + analysis.loudness);
            
            // 6. Create audio fingerprints
            System.out.println("6. Creating audio fingerprints...");
            AudioAnalyzerAdvanced.AudioFingerprint fp1 = processor.createFingerprint(sineWave);
            AudioAnalyzerAdvanced.AudioFingerprint fp2 = processor.createFingerprint(echoSine);
            double similarity = processor.compareFingerprints(fp1, fp2);
            System.out.println("   Fingerprint similarity: " + similarity);
            
            // 7. Generate visualizations
            System.out.println("7. Generating visualizations...");
            
            // Waveform visualization
            AdvancedWaveformParams waveformParams = new AdvancedWaveformParams();
            waveformParams.width = 1024;
            waveformParams.height = 512;
            waveformParams.style = AdvancedWaveformParams.VisualizationStyle.AREA;
            waveformParams.showSpectrum = true;
            BufferedImage waveform = processor.generateWaveform(sineWave, waveformParams);
            System.out.println("   Generated waveform visualization: " + waveform.getWidth() + "x" + waveform.getHeight());
            
            // 3D visualization
            Audio3DParams params3D = new Audio3DParams();
            params3D.width = 800;
            params3D.height = 600;
            params3D.rotationY = Math.PI / 4;
            BufferedImage visualization3D = processor.generate3DVisualization(sineWave, params3D);
            System.out.println("   Generated 3D visualization: " + visualization3D.getWidth() + "x" + visualization3D.getHeight());
            
            // Spectrogram
            SpectrogramParams spectrogramParams = new SpectrogramParams();
            spectrogramParams.width = 1024;
            spectrogramParams.height = 512;
            BufferedImage spectrogram = processor.generateSpectrogram(sineWave, spectrogramParams);
            System.out.println("   Generated spectrogram: " + spectrogram.getWidth() + "x" + spectrogram.getHeight());
            
            // 8. Create animation
            System.out.println("8. Creating animation...");
            BufferedImage[] frames = processor.createAnimation(sineWave, waveformParams, 30);
            System.out.println("   Generated animation with " + frames.length + " frames");
            
            // 9. Asynchronous processing
            System.out.println("9. Performing asynchronous processing...");
            CompletableFuture<AudioEngine.AudioClip> asyncResult = processor.processAsync(proc -> {
                // Simulate complex processing
                try {
                    Thread.sleep(1000); // Simulate processing time
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return proc.applyEffect(sineWave, AudioEffect.FLANGER, 0.4, 3.0);
            });
            
            AudioEngine.AudioClip asyncClip = asyncResult.get();
            System.out.println("   Async processing completed: " + asyncClip.getName());
            
            // 10. Batch processing example
            System.out.println("10. Performing batch processing...");
            List<AudioProcessor.ProcessingStep> steps = Arrays.asList(
                new AudioProcessor.GenerateClipStep("batch_sine", WaveForm.SINE, 880.0, 1.0),
                new AudioProcessor.ApplyEffectStep(AudioEffect.ECHO, 0.1, 0.9),
                new AudioProcessor.ApplyEffectStep(AudioEffect.DISTORTION, 3.0, 0.5)
            );
            
            AudioProcessor.BatchProcessingTask batchTask = new AudioProcessor.BatchProcessingTask(steps);
            AudioEngine.AudioClip batchResult = batchTask.process(processor);
            System.out.println("   Batch processing completed: " + batchResult.getName());
            
            // 11. Play audio (simulated)
            System.out.println("11. Playing audio...");
            processor.play(sineWave);
            processor.play3D(sineWave, 10, 0, 5);
            
            // 12. Save to file (simulated)
            System.out.println("12. Saving audio to file...");
            processor.saveToFile(sineWave, "output/sine_wave.wav");
            
            // 13. Show cache statistics
            System.out.println("13. Audio processor statistics:");
            System.out.println("   - Cached clips: " + 0); // Would need access to private cache
            System.out.println("   - Engine initialized: true");
            
            // 14. Cleanup
            System.out.println("14. Cleaning up...");
            processor.shutdown();
            
            System.out.println("=== Demo completed successfully! ===");
            
        } catch (Exception e) {
            System.err.println("Error in audio demo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            processor.shutdown();
        }
    }
}