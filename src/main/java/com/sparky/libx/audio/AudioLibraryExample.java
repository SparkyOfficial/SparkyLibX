package com.sparky.libx.audio;

import com.sparky.libx.audio.AudioEngine.AudioClip;
import com.sparky.libx.audio.AudioEngine.AudioSource;
import com.sparky.libx.audio.AudioAnalyzer.AnalysisResult;

/**
 * example demonstrating how to use the sparky audio library
 * shows various features including loading, playing, effects, and analysis
 * @author Андрій Будильников
 */
public class AudioLibraryExample {
    
    public static void main(String[] args) {
        try {
            // create audio library instance
            AudioLibrary audioLib = new AudioLibrary();
            
            System.out.println("=== Sparky Audio Library Example ===");
            
            // generate some test tones
            System.out.println("1. Generating test tones...");
            AudioClip sineWave = audioLib.generateSineWave("sine_440hz", 440.0, 2.0); // A4 note
            AudioClip squareWave = audioLib.generateSquareWave("square_220hz", 220.0, 2.0); // A3 note
            AudioClip whiteNoise = audioLib.generateWhiteNoise("noise", 1.0);
            
            System.out.println("Generated sine wave: " + sineWave);
            System.out.println("Generated square wave: " + squareWave);
            System.out.println("Generated white noise: " + whiteNoise);
            
            // mix audio clips
            System.out.println("\n2. Mixing audio clips...");
            AudioClip mixed = audioLib.mixClips(sineWave, 0.7, squareWave, 0.3, "mixed_tones");
            System.out.println("Mixed audio: " + mixed);
            
            // apply audio effects
            System.out.println("\n3. Applying audio effects...");
            AudioClip reverbSound = audioLib.applyReverb(mixed, 0.5, 0.3, 1.0, 0.4, "reverb_mix");
            AudioClip echoSound = audioLib.applyEcho(sineWave, 0.3, 0.5, 0.7, "echo_sine");
            AudioClip distortedSound = audioLib.applyDistortion(squareWave, 2.0, 0.5, 0.8, "distorted_square");
            
            System.out.println("Applied reverb effect");
            System.out.println("Applied echo effect");
            System.out.println("Applied distortion effect");
            
            // analyze audio
            System.out.println("\n4. Analyzing audio...");
            AnalysisResult analysis = audioLib.analyze(sineWave);
            System.out.println("Sine wave analysis:");
            System.out.println("  Duration: " + String.format("%.2f", analysis.duration) + " seconds");
            System.out.println("  Average amplitude: " + String.format("%.3f", analysis.averageAmplitude));
            System.out.println("  Max amplitude: " + String.format("%.3f", analysis.maxAmplitude));
            System.out.println("  RMS: " + String.format("%.3f", analysis.rms));
            
            // get additional analysis
            double tempo = audioLib.calculateTempo(sineWave);
            double dominantFreq = audioLib.getDominantFrequency(sineWave);
            double quality = audioLib.getQualityScore(sineWave);
            
            System.out.println("  Tempo: " + String.format("%.1f", tempo) + " BPM");
            System.out.println("  Dominant frequency: " + String.format("%.1f", dominantFreq) + " Hz");
            System.out.println("  Quality score: " + String.format("%.3f", quality));
            
            // 3D audio positioning
            System.out.println("\n5. 3D audio positioning...");
            AudioSource source1 = audioLib.play3D(sineWave, 5.0, 0.0, 0.0);
            AudioSource source2 = audioLib.play3D(squareWave, -5.0, 0.0, 0.0);
            
            System.out.println("Playing sine wave at position (5, 0, 0)");
            System.out.println("Playing square wave at position (-5, 0, 0)");
            
            // set listener position and orientation
            audioLib.setListenerPosition(0.0, 0.0, 0.0);
            audioLib.setListenerOrientation(0.0, 0.0, 1.0, 0.0, 1.0, 0.0); // facing +Z, up +Y
            
            System.out.println("Listener at origin, facing +Z direction");
            
            // update audio engine
            audioLib.update(0.016); // ~60 FPS update
            
            // 2D stereo positioning
            System.out.println("\n6. 2D stereo positioning...");
            AudioSource leftSource = audioLib.play2D(sineWave, -0.8); // mostly left
            AudioSource rightSource = audioLib.play2D(squareWave, 0.8); // mostly right
            
            System.out.println("Playing sine wave panned to left (-0.8)");
            System.out.println("Playing square wave panned to right (0.8)");
            
            // volume control
            System.out.println("\n7. Volume control...");
            audioLib.setMasterVolume(0.8);
            System.out.println("Master volume set to 80%");
            
            double currentVolume = audioLib.getMasterVolume();
            System.out.println("Current master volume: " + String.format("%.1f", currentVolume));
            
            // audio visualization
            System.out.println("\n8. Audio visualization...");
            
            // generate ascii waveform
            String asciiWave = audioLib.generateAsciiWaveform(sineWave, 40, 10);
            System.out.println("ASCII waveform representation:");
            System.out.println(asciiWave);
            
            // generate text spectrum
            String textSpectrum = audioLib.generateTextSpectrum(sineWave, 10);
            System.out.println("Text spectrum analysis:");
            System.out.println(textSpectrum);
            
            // format conversion examples
            System.out.println("\n9. Audio format conversion...");
            // in a real application, you would have different format audio data to convert
            
            // recording example (commented out as it requires microphone access)
            /*
            System.out.println("\n10. Audio recording...");
            audioLib.startRecording();
            System.out.println("Recording started...");
            Thread.sleep(3000); // record for 3 seconds
            AudioClip recorded = audioLib.stopRecording("recorded_sample");
            System.out.println("Recording stopped. Recorded: " + recorded);
            */
            
            // crossfade example
            System.out.println("\n11. Crossfade example...");
            AudioClip crossfaded = audioLib.crossfadeClips(sineWave, squareWave, 0.5, "crossfaded");
            System.out.println("Created crossfaded audio: " + crossfaded);
            
            // fade in/out example
            System.out.println("\n12. Fade in/out example...");
            AudioClip fadedIn = audioLib.fadeIn(sineWave, 0.5, "fade_in");
            AudioClip fadedOut = audioLib.fadeOut(sineWave, 0.5, "fade_out");
            System.out.println("Created fade-in audio: " + fadedIn);
            System.out.println("Created fade-out audio: " + fadedOut);
            
            // filter examples
            System.out.println("\n13. Filter examples...");
            AudioClip lowPass = audioLib.applyLowPassFilter(sineWave, 1000.0, "lowpass");
            AudioClip highPass = audioLib.applyHighPassFilter(squareWave, 500.0, "highpass");
            System.out.println("Applied low-pass filter (1000Hz cutoff)");
            System.out.println("Applied high-pass filter (500Hz cutoff)");
            
            // modulation effects
            System.out.println("\n14. Modulation effects...");
            AudioClip chorus = audioLib.applyChorus(sineWave, 0.3, 1.0, 0.5, "chorus");
            AudioClip flanger = audioLib.applyFlanger(sineWave, 0.2, 2.0, 0.7, "flanger");
            System.out.println("Applied chorus effect");
            System.out.println("Applied flanger effect");
            
            // beat detection
            System.out.println("\n15. Beat detection...");
            double[] beats = audioLib.detectBeats(sineWave);
            System.out.println("Detected " + beats.length + " beats in sine wave");
            
            // cleanup
            System.out.println("\n16. Cleaning up...");
            audioLib.stop(); // stop playback
            audioLib.shutdown(); // shutdown library
            
            System.out.println("\n=== Example completed successfully ===");
            
        } catch (Exception e) {
            System.err.println("Error in audio library example: " + e.getMessage());
            e.printStackTrace();
        }
    }
}