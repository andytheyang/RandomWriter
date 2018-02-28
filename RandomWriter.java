import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Random;

public class RandomWriter {
    private String filename;
    private HashMap<String, StringBuffer> probs;
    private int k;
    private Random rand;

    public RandomWriter(String filename) {
        this.filename = filename;
        this.rand = new Random();
    }

    public boolean doAnalysis(int k) throws IOException {
        this.k = k;
        probs = new HashMap<>();
        FileReader fs = new FileReader(filename);
        StringBuffer window = new StringBuffer();

        /*
         * Prepare first window
         */
        for (int i = 0; i < k; i++) {
            if (fs.ready()) {
                window.append((char) fs.read());
            } else {
                fs.close();                
                return false;
            }
        }

        /*
         * Calculate probabilities
         */
        while (fs.ready()) {
            char nextChar = (char) fs.read();

            if (nextChar == '\r' || nextChar == '\n')
                nextChar = ' ';

            window.append(nextChar);
            String currentKey = window.substring(0, k);   // get all but last character
            
            if (probs.containsKey(currentKey)) {
                probs.get(currentKey).append((char) window.charAt(k));   // get last character and append
            } else {
                probs.put(currentKey, new StringBuffer().append((char) window.charAt(k)));
            }
            
            // System.out.println(currentKey + " - " + probs.get(currentKey).toString());
            window.delete(0, 1);
        }

        fs.close();

        return true;
    }

    public String write(int length) {
        StringBuffer output = new StringBuffer(getSeed());     // hardcoded initial seed
        int charsRemaining = length - k;

        for (int i = 0; i < charsRemaining; i++) {
            StringBuffer prob = probs.get(output.substring(output.length() - k, output.length()));
            
            if (prob == null)     // we got unlucky and hit the one end case
                break;
            
            char nextChar = prob.charAt(rand.nextInt(prob.length()));
            output.append(nextChar);
            // System.out.println(output);
        }

        return output.toString();
    }

    private String getSeed() {
        List<String> keys = new ArrayList<>(probs.keySet());
        return keys.get(rand.nextInt(keys.size()));
    }

    public static void main(String[] args) {
        RandomWriter r = new RandomWriter(args[0]);
        int k = Integer.parseInt(args[1]);
        int len = Integer.parseInt(args[2]);

        try {
            if (r.doAnalysis(k))        // if analysis succeeds
                System.out.println(r.write(len));
        } catch (IOException e) {
            System.err.println("IO Error occurred");
            e.printStackTrace(System.err);
        }
    }
}