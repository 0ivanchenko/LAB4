import java.util.Arrays;

// Класс AudioBuffer
class AudioBuffer {
    private float[] data; // массив данных аудиосигнала
    private int size; // размер буфера (кол-во сэмплов)
    private int sampleRate; // частота дискретизации

    public AudioBuffer() {
        this.data = null;
        this.size = 0;
        this.sampleRate = 0;
    }

    public void init(int size, int sampleRate) {
        this.data = new float[size];
        this.size = size;
        this.sampleRate = sampleRate;
    }

    public float[] getData() {
        return data;
    }

    public int getSize() {
        return size;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void freeBuffer() {
        data = null;
    }
}

// Абстрактный класс AudioEffect
abstract class AudioEffect {
    protected float mix; // коэффициент смешивания сухого и обработанного сигнала

    public AudioEffect() {
        this.mix = 0.0f;
    }

    public void setMix(float mix) {
        this.mix = mix;
    }

    public float getMix() {
        return mix;
    }

    public abstract void applyEffect(AudioBuffer input, AudioBuffer output);
}

// Класс Reverb (Реверберация)
class Reverb extends AudioEffect {
    private float roomSize; // размер комнаты для реверберации
    private float dampening; // уровень демпфирования

    public Reverb() {
        this.roomSize = 0.0f;
        this.dampening = 0.0f;
    }

    public void init(float roomSize, float dampening, float mix) {
        this.roomSize = roomSize;
        this.dampening = dampening;
        this.mix = mix;
    }

    @Override
    public void applyEffect(AudioBuffer input, AudioBuffer output) {
        int bufferSize = input.getSize();
        float[] delayBuffer1 = new float[bufferSize];
        float[] delayBuffer2 = new float[bufferSize];

        Arrays.fill(delayBuffer1, 0.0f);
        Arrays.fill(delayBuffer2, 0.0f);

        for (int i = 0; i < bufferSize; i++) {
            int delayIndex1 = (i + (int) (roomSize * input.getSampleRate())) % bufferSize;
            int delayIndex2 = (i + (int) ((roomSize + 0.1f) * input.getSampleRate())) % bufferSize;

            float delayedSample1 = delayBuffer1[delayIndex1];
            float delayedSample2 = delayBuffer2[delayIndex2];

            float outputSample = input.getData()[i] + dampening * (delayedSample1 + delayedSample2);

            delayBuffer1[i] = input.getData()[i] + dampening * delayedSample1;
            delayBuffer2[i] = input.getData()[i] + dampening * delayedSample2;

            output.getData()[i] = outputSample;
        }
    }
}

// Класс Delay (Задержка)
class Delay extends AudioEffect {
    private float delayTime; // время задержки в миллисекундах
    private float feedback; // уровень обратной связи

    public Delay() {
        this.delayTime = 0.0f;
        this.feedback = 0.0f;
    }

    public void init(float delayTime, float feedback, float mix) {
        this.delayTime = delayTime;
        this.feedback = feedback;
        this.mix = mix;
    }

    @Override
    public void applyEffect(AudioBuffer input, AudioBuffer output) {
        int bufferSize = input.getSize();
        float[] delayBuffer = new float[bufferSize];

        Arrays.fill(delayBuffer, 0.0f);

        for (int i = 0; i < bufferSize; i++) {
            int delayIndex = (i + (int) (delayTime * input.getSampleRate() / 1000.0f)) % bufferSize;

            float delayedSample = delayBuffer[delayIndex];
            float outputSample = input.getData()[i] + feedback * delayedSample;

            delayBuffer[i] = input.getData()[i] + feedback * delayedSample;

            output.getData()[i] = outputSample;
        }
    }
}

// Класс AudioFile
class AudioFile {
    private String filePath;

    public AudioFile(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}

// Класс AudioInput
class AudioInput {
    private AudioBuffer buffer;
    private AudioFile source;

    public AudioInput() {
        this.buffer = new AudioBuffer();
        this.source = null;
    }

    public void init(int size, int sampleRate, AudioFile source) {
        buffer.init(size, sampleRate);
        this.source = source;
    }

    public AudioBuffer getBuffer() {
        return buffer;
    }

    public AudioFile getSource() {
        return source;
    }

    public void freeInput() {
        buffer.freeBuffer();
    }
}

// Класс AudioOutput
class AudioOutput {
    private AudioBuffer buffer;
    private AudioFile destination;

    public AudioOutput() {
        this.buffer = new AudioBuffer();
        this.destination = null;
    }

    public void init(int size, int sampleRate, AudioFile destination) {
        buffer.init(size, sampleRate);
        this.destination = destination;
    }

    public AudioBuffer getBuffer() {
        return buffer;
    }

    public AudioFile getDestination() {
        return destination;
    }

    public void freeOutput() {
        buffer.freeBuffer();
    }
}

// Класс PluginSettings
class PluginSettings {
    private float gain; // усиление сигнала
    private boolean bypass; // флаг включения/выключения эффекта

    public PluginSettings() {
        this.gain = 1.0f;
        this.bypass = false;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public void setBypass(boolean bypass) {
        this.bypass = bypass;
    }

    public float getGain() {
        return gain;
    }

    public boolean isBypass() {
        return bypass;
    }
}

// Класс AudioPlugin
class AudioPlugin {
    private AudioInput input;
    private AudioOutput output;
    private AudioEffect[] effects;
    private PluginSettings settings;

    public AudioPlugin() {
        this.input = new AudioInput();
        this.output = new AudioOutput();
        this.effects = null;
        this.settings = new PluginSettings();
    }

    public void init(int inputSize, int inputSampleRate, AudioFile inputSource,
                     int outputSize, int outputSampleRate, AudioFile outputDestination,
                     AudioEffect[] effects) {
        input.init(inputSize, inputSampleRate, inputSource);
        output.init(outputSize, outputSampleRate, outputDestination);
        this.effects = effects;
    }

    public void applyEffects() {
        for (AudioEffect effect : effects) {
            effect.applyEffect(input.getBuffer(), output.getBuffer());
        }
    }

    public void freePlugin() {
        input.freeInput();
        output.freeOutput();
    }

    public PluginSettings getSettings() {
        return settings;
    }
}

// Главный класс
public class Main {
    public static void main(String[] args) {
        // Создание эффектов
        AudioEffect[] effects = new AudioEffect[2];
        effects[0] = new Reverb();
        effects[1] = new Delay();

        // Инициализация эффектов
        ((Reverb) effects[0]).init(0.8f, 0.5f, 0.7f);
        ((Delay) effects[1]).init(500.0f, 0.4f, 0.6f);

        // Создание объектов AudioFile
        AudioFile inputFile = new AudioFile("input.wav");
        AudioFile outputFile = new AudioFile("output.wav");

        // Создание и настройка плагина
        AudioPlugin plugin = new AudioPlugin();
        plugin.init(1024, 44100, inputFile, 1024, 44100, outputFile, effects);

        // Применение эффектов
        plugin.applyEffects();

        // Освобождение ресурсов
        plugin.freePlugin();
    }
}