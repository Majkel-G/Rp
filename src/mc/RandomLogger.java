// Balíček s triedou
package mc;

import java.io.*;
import java.util.*;

public class RandomLogger {
    // Režim generovania: RANDOM = generuj nové, REPLAY = čítaj zo záznamu
    public enum Mode {RANDOM, REPLAY}

    private final Mode mode;
    private final Random random;            // RNG generátor pre RANDOM režim
    private final BufferedReader reader;     // Čítač súboru pre REPLAY režim
    private final PrintWriter writer;        // Zapisovač logu pre RANDOM režim
    private long index = 0;                  // Počítadlo vygenerovaných/načítaných hodnôt

    // Konštruktor inicializuje podľa režimu a nastaví súbor pre log/replay
    public RandomLogger(Mode mode, String file) throws IOException {
        this.mode = mode;
        File f = new File(file); // Súbor pre ukladanie/čítanie logu

        if (mode == Mode.RANDOM) {
            this.random = new Random();
            this.reader = null;
            this.writer = new PrintWriter(new FileWriter(f, false)); // Prepíš log súbor
            writer.println("#RNG LOG START"); // Hlavička logu
            writer.flush();
        } else {
            this.random = null;
            this.reader = new BufferedReader(new FileReader(f)); // Otvor log pre replay
            this.writer = null;
        }
    }

    // Vráti náhodnú/zalogovanú Gauss hodnotu
    public double nextGaussian() throws IOException {
        double v;
        if (mode == Mode.RANDOM) {
            v = random.nextGaussian();
            writer.println(v); // Zaloguj hodnotu
        } else {
            String line;
            do {
                line = reader.readLine();
                if (line == null) throw new EOFException("Random log ended early at index=" + index);
            } while (line.startsWith("#") || line.trim().isEmpty()); // Preskoč komentáre/prázdne
            v = Double.parseDouble(line);
        }
        index++;
        return v;
    }

    // Vráti náhodný double
    public double nextDouble() throws IOException {
        double v;
        if (mode == Mode.RANDOM) {
            v = random.nextDouble();
            writer.println(v); // Zaloguj
        } else {
            String line;
            do {
                line = reader.readLine();
                if (line == null) throw new EOFException("Random log ended early at index=" + index);
            } while (line.startsWith("#") || line.trim().isEmpty());
            v = Double.parseDouble(line);
        }
        index++;
        return v;
    }

    // Vráti náhodný/zalogovaný int v rozsahu <0, bound)
    public int nextInt(int bound) throws IOException {
        int v;
        if (mode == Mode.RANDOM) {
            v = random.nextInt(bound);
            writer.println(v); // Zaloguj
        } else {
            String line;
            do {
                line = reader.readLine();
                if (line == null) throw new EOFException("Random log ended early at index=" + index);
            } while (line.startsWith("#") || line.trim().isEmpty());
            v = Integer.parseInt(line.trim());
        }
        index++;
        return v;
    }

    // Vráti aktuálny index (počet hodnôt)
    public long getIndex() {
        return index;
    }

    // Zatvorí zdroje
    public void close() {
        try {
            if (reader != null) reader.close();
        } catch (Exception ignored) {}
        if (writer != null) writer.close();
    }
}
