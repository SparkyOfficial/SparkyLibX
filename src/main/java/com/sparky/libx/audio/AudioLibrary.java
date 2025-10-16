package com.sparky.libx.audio;

import com.sparky.libx.audio.AudioEngine.AudioClip;
import com.sparky.libx.audio.AudioEngine.AudioSource;
import com.sparky.libx.audio.AudioEngine.AudioListener;
import com.sparky.libx.audio.AudioEngine.Engine;
import javax.sound.sampled.AudioFormat;
import java.util.List;

/**
 * main audio library class that provides a unified interface to all audio functionality
 * combines all audio components into a single, easy-to-use library
 * @author Андрій Будильников
 */
public class AudioLibrary {
    
    private final Engine audioEngine;
    private final AudioPlayer audioPlayer;
    private final AudioRecorder audioRecorder;
    
    public AudioLibrary() {
        this.audioEngine = new Engine();
        this.audioPlayer = new AudioPlayer();
        this.audioRecorder = new AudioRecorder();
        this.audioEngine.initialize();
    }
    
    /**
     * load an audio clip from file
     */
    public AudioClip loadClip(String filePath, String name) throws Exception {
        return AudioFileLoader.loadAudioClip(filePath, name);
    }
    
    /**
     * load an audio clip from resource
     */
    public AudioClip loadResource(String resourcePath, String name) throws Exception {
        return AudioFileLoader.loadAudioResource(resourcePath, name);
    }
    
    /**
     * generate a sine wave tone
     */
    public AudioClip generateSineWave(String name, double frequency, double duration) {
        return AudioEngine.AudioSynthesizer.generateSineWave(name, frequency, duration, 44100);
    }
    
    /**
     * generate a square wave tone
     */
    public AudioClip generateSquareWave(String name, double frequency, double duration) {
        return AudioEngine.AudioSynthesizer.generateSquareWave(name, frequency, duration, 44100);
    }
    
    /**
     * generate white noise
     */
    public AudioClip generateWhiteNoise(String name, double duration) {
        return AudioEngine.AudioSynthesizer.generateWhiteNoise(name, duration, 44100);
    }
    
    /**
     * play an audio clip
     */
    public void playClip(AudioClip clip) throws Exception {
        audioPlayer.play(clip);
    }
    
    /**
     * play an audio clip with 3D positioning
     */
    public AudioSource play3D(AudioClip clip, double x, double y, double z) {
        return audioEngine.play3DSound(clip.getName(), new com.sparky.libx.math.Vector3D(x, y, z));
    }
    
    /**
     * play an audio clip with 2D stereo positioning
     */
    public AudioSource play2D(AudioClip clip, double pan) {
        return audioEngine.play2DSound(clip.getName(), pan);
    }
    
    /**
     * set master volume (0.0 to 1.0)
     */
    public void setMasterVolume(double volume) {
        audioEngine.setMasterVolume(volume);
        audioPlayer.setVolume(volume);
    }
    
    /**
     * get master volume
     */
    public double getMasterVolume() {
        return audioEngine.getMasterVolume();
    }
    
    /**
     * pause playback
     */
    public void pause() {
        audioPlayer.pause();
    }
    
    /**
     * resume playback
     */
    public void resume() {
        audioPlayer.resume();
    }
    
    /**
     * stop playback
     */
    public void stop() {
        audioPlayer.stop();
    }
    
    /**
     * start recording audio
     */
    public void startRecording() throws Exception {
        audioRecorder.start();
    }
    
    /**
     * stop recording and get recorded audio
     */
    public AudioClip stopRecording(String name) {
        audioRecorder.stop();
        return audioRecorder.getRecordedAudio(name);
    }
    
    /**
     * pause recording
     */
    public void pauseRecording() {
        audioRecorder.pause();
    }
    
    /**
     * resume recording
     */
    public void resumeRecording() {
        audioRecorder.resume();
    }
    
    /**
     * check if currently recording
     */
    public boolean isRecording() {
        return audioRecorder.isRecording();
    }
    
    /**
     * mix multiple audio clips together
     */
    public AudioClip mixClips(List<AudioClip> clips, String name) throws Exception {
        return AudioMixerUtil.mixClips(clips, name);
    }
    
    /**
     * mix two audio clips with specified volumes
     */
    public AudioClip mixClips(AudioClip clip1, double volume1, AudioClip clip2, double volume2, String name) throws Exception {
        return AudioMixerUtil.mixClips(clip1, volume1, clip2, volume2, name);
    }
    
    /**
     * crossfade two audio clips
     */
    public AudioClip crossfadeClips(AudioClip clip1, AudioClip clip2, double crossfadeDuration, String name) throws Exception {
        return AudioMixerUtil.crossfadeClips(clip1, clip2, crossfadeDuration, name);
    }
    
    /**
     * fade in an audio clip
     */
    public AudioClip fadeIn(AudioClip clip, double fadeDuration, String name) throws Exception {
        return AudioMixerUtil.fadeIn(clip, fadeDuration, name);
    }
    
    /**
     * fade out an audio clip
     */
    public AudioClip fadeOut(AudioClip clip, double fadeDuration, String name) throws Exception {
        return AudioMixerUtil.fadeOut(clip, fadeDuration, name);
    }
    
    /**
     * apply reverb effect
     */
    public AudioClip applyReverb(AudioClip clip, double roomSize, double damping, double width, double level, String name) {
        return AudioEffectsProcessor.applyReverb(clip, roomSize, damping, width, level, name);
    }
    
    /**
     * apply echo effect
     */
    public AudioClip applyEcho(AudioClip clip, double delay, double decay, double feedback, String name) {
        return AudioEffectsProcessor.applyEcho(clip, delay, decay, feedback, name);
    }
    
    /**
     * apply distortion effect
     */
    public AudioClip applyDistortion(AudioClip clip, double gain, double threshold, double mix, String name) {
        return AudioEffectsProcessor.applyDistortion(clip, gain, threshold, mix, name);
    }
    
    /**
     * apply chorus effect
     */
    public AudioClip applyChorus(AudioClip clip, double depth, double rate, double mix, String name) {
        return AudioEffectsProcessor.applyChorus(clip, depth, rate, mix, name);
    }
    
    /**
     * apply flanger effect
     */
    public AudioClip applyFlanger(AudioClip clip, double depth, double rate, double feedback, String name) {
        return AudioEffectsProcessor.applyFlanger(clip, depth, rate, feedback, name);
    }
    
    /**
     * apply low-pass filter
     */
    public AudioClip applyLowPassFilter(AudioClip clip, double cutoffFrequency, String name) {
        return AudioEffectsProcessor.applyLowPassFilter(clip, cutoffFrequency, name);
    }
    
    /**
     * apply high-pass filter
     */
    public AudioClip applyHighPassFilter(AudioClip clip, double cutoffFrequency, String name) {
        return AudioEffectsProcessor.applyHighPassFilter(clip, cutoffFrequency, name);
    }
    
    /**
     * analyze audio clip
     */
    public AudioAnalyzer.AnalysisResult analyze(AudioClip clip) {
        return AudioAnalyzer.analyze(clip);
    }
    
    /**
     * detect beats in audio clip
     */
    public double[] detectBeats(AudioClip clip) {
        return AudioAnalyzer.detectBeats(clip);
    }
    
    /**
     * calculate audio tempo
     */
    public double calculateTempo(AudioClip clip) {
        return AudioAnalyzer.calculateTempo(clip);
    }
    
    /**
     * get dominant frequency
     */
    public double getDominantFrequency(AudioClip clip) {
        return AudioAnalyzer.getDominantFrequency(clip);
    }
    
    /**
     * get audio quality score
     */
    public double getQualityScore(AudioClip clip) {
        return AudioAnalyzer.getQualityScore(clip);
    }
    
    /**
     * generate waveform image
     */
    public java.awt.image.BufferedImage generateWaveform(AudioClip clip, AudioVisualizer.WaveformParams params) {
        return AudioVisualizer.generateWaveform(clip, params);
    }
    
    /**
     * generate spectrum image
     */
    public java.awt.image.BufferedImage generateSpectrum(AudioClip clip, AudioVisualizer.SpectrumParams params) {
        return AudioVisualizer.generateSpectrum(clip, params);
    }
    
    /**
     * generate ascii waveform
     */
    public String generateAsciiWaveform(AudioClip clip, int width, int height) {
        return AudioVisualizer.generateAsciiWaveform(clip, width, height);
    }
    
    /**
     * generate text spectrum
     */
    public String generateTextSpectrum(AudioClip clip, int bands) {
        return AudioVisualizer.generateTextSpectrum(clip, bands);
    }
    
    /**
     * convert audio format
     */
    public byte[] convertFormat(byte[] audioData, AudioFormat sourceFormat, AudioFormat targetFormat) throws Exception {
        return AudioFormatConverter.convertFormat(audioData, sourceFormat, targetFormat);
    }
    
    /**
     * resample audio
     */
    public byte[] resample(byte[] audioData, AudioFormat sourceFormat, float targetSampleRate) throws Exception {
        return AudioFormatConverter.resample(audioData, sourceFormat, targetSampleRate);
    }
    
    /**
     * change bit depth
     */
    public byte[] changeBitDepth(byte[] audioData, AudioFormat sourceFormat, int targetBitDepth) throws Exception {
        return AudioFormatConverter.changeBitDepth(audioData, sourceFormat, targetBitDepth);
    }
    
    /**
     * convert mono to stereo
     */
    public byte[] monoToStereo(byte[] audioData, AudioFormat sourceFormat) throws Exception {
        return AudioFormatConverter.monoToStereo(audioData, sourceFormat);
    }
    
    /**
     * convert stereo to mono
     */
    public byte[] stereoToMono(byte[] audioData, AudioFormat sourceFormat) throws Exception {
        return AudioFormatConverter.stereoToMono(audioData, sourceFormat);
    }
    
    /**
     * get audio engine listener
     */
    public AudioListener getListener() {
        return audioEngine.getListener();
    }
    
    /**
     * set audio engine listener position
     */
    public void setListenerPosition(double x, double y, double z) {
        audioEngine.getListener().setPosition(new com.sparky.libx.math.Vector3D(x, y, z));
    }
    
    /**
     * set audio engine listener orientation
     */
    public void setListenerOrientation(double forwardX, double forwardY, double forwardZ, 
                                     double upX, double upY, double upZ) {
        AudioListener listener = audioEngine.getListener();
        listener.setForward(new com.sparky.libx.math.Vector3D(forwardX, forwardY, forwardZ));
        listener.setUp(new com.sparky.libx.math.Vector3D(upX, upY, upZ));
    }
    
    /**
     * update audio engine
     */
    public void update(double deltaTime) {
        audioEngine.update(deltaTime);
    }
    
    /**
     * shutdown audio library
     */
    public void shutdown() {
        audioEngine.shutdown();
        audioPlayer.close();
        audioRecorder.close();
    }
    
    /**
     * get audio engine
     */
    public Engine getAudioEngine() {
        return audioEngine;
    }
    
    /**
     * get audio player
     */
    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }
    
    /**
     * get audio recorder
     */
    public AudioRecorder getAudioRecorder() {
        return audioRecorder;
    }
}